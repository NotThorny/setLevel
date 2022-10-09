package thorny.grasscutters.setLevel.commands;

import java.util.List;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.Command.TargetRequirement;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketAvatarAddNotify;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;
import emu.grasscutter.utils.Position;

// Command usage
@Command(label = "level", aliases = "lv", usage = "[level]", targetRequirement = TargetRequirement.NONE)
public class setLevelCommand implements CommandHandler {
	@Override
	public void execute(Player sender, Player targetPlayer, List<String> args) {

		if (!(args.size() < 1)) {
			switch (args.get(0)) {
				case "all":
					try {
						Integer.parseInt(args.get(1));
					} catch (Exception e) {
						this.sendUsageMessage(sender);
						break;
					}
					allLevel(sender, targetPlayer, args);
					break;
				case "team":
					try {
						Integer.parseInt(args.get(1));
					} catch (Exception e) {
						this.sendUsageMessage(sender);
						break;
					}
					teamLevel(sender, targetPlayer, args);
					break;
				default:
					try {
						activeLevel(sender, targetPlayer, args);
					} catch (NumberFormatException e) {
						this.sendUsageMessage(sender);
						throw e;
					}
			}// switch
		} // if
		else {
			this.sendUsageMessage(sender);
		} // else
	}// execute

	public void teamLevel(Player sender, Player targetPlayer, List<String> args) {
		int scene = targetPlayer.getSceneId();

		targetPlayer.getTeamManager().getActiveTeam().forEach(entity -> {
			Avatar avatar = entity.getAvatar();
			int level = Integer.parseInt(args.get(1));
			level = checkLevel(level);
			setAvatar(sender, avatar, level);
		});

		reloadLevel(targetPlayer, scene, args);
	}

	private void activeLevel(Player sender, Player targetPlayer, List<String> args) {
		Avatar avatar = targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar();
		int scene = targetPlayer.getSceneId();
		int level = Integer.parseInt(args.get(0));
		level = checkLevel(level);
		setAvatar(sender, avatar, level);

		reloadLevel(targetPlayer, scene, args);
	}

	public void allLevel(Player sender, Player targetPlayer, List<String> args) {
		int scene = targetPlayer.getSceneId();

		List<Avatar> avatars = DatabaseHelper.getAvatars(sender);
		for (Avatar avatar : avatars) {
			avatar = sender.getAvatars().getAvatarById(avatar.getAvatarId());
			int level = Integer.parseInt(args.get(1));
			level = checkLevel(level);
			setAvatar(sender, avatar, level);
		}
		reloadLevel(targetPlayer, scene, args);
	}

	private int checkLevel(int level) {
		if (level < 1) level = 1;
        if (level > 90) level = 90;
		return level;
	} // checkLevel

	private void setAvatar(Player sender, Avatar avatar, int level) {
		avatar.setPromoteLevel(Avatar.getMinPromoteLevel(level));
		avatar.setLevel(level);
		avatar.recalcStats();
		avatar.save();
		sender.sendPacket(new PacketAvatarAddNotify(avatar, false));
	} // setAvatar

	public void reloadLevel(Player targetPlayer, int scene, List<String> args) {
		try {
			Position targetPlayerPos = targetPlayer.getPosition();
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, 1, targetPlayerPos);
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, scene, targetPlayerPos);
			targetPlayer.getScene().broadcastPacket(new PacketSceneEntityAppearNotify(targetPlayer));

			switch (args.get(0)) {
				case "all":
					CommandHandler.sendMessage(targetPlayer, "Changed all character levels!");
					break;
				case "team":
					CommandHandler.sendMessage(targetPlayer, "Changed team levels!");
					break;
				default:
					CommandHandler.sendMessage(targetPlayer, "Changed level!");
			}// switch
		} catch (Exception e) {
			CommandHandler.sendMessage(targetPlayer, "Failed to reload! Relog to apply changes.");
		} // catch
	}// reloadLevel
}// setLevelCommand
