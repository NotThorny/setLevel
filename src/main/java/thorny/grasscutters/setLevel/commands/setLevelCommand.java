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
import emu.grasscutter.game.world.Position;

// Command usage
@Command(label = "level", aliases = "lv", usage = "[level]", targetRequirement = TargetRequirement.NONE)
public class setLevelCommand implements CommandHandler {
	public int level;

	@Override
	public void execute(Player sender, Player targetPlayer, List<String> args) {

		if (!(args.size() < 1)) {
			try {
				switch (args.get(0)) {
					case "all" -> allLevel(sender, targetPlayer, args);
					case "team" -> teamLevel(sender, targetPlayer, args);
					default -> activeLevel(sender, targetPlayer, args);
				} // switch
			} catch (Exception e) {
				this.sendUsageMessage(sender);
				return;
			}
			reloadLevel(targetPlayer, args);
		} else this.sendUsageMessage(sender);
	}// execute

	public void teamLevel(Player sender, Player targetPlayer, List<String> args) {
		level = checkLevel(Integer.parseInt(args.get(1)));

		targetPlayer.getTeamManager().getActiveTeam().forEach(entity -> {
			Avatar avatar = entity.getAvatar();
			setAvatar(sender, avatar, level);
		});
	}

	private void activeLevel(Player sender, Player targetPlayer, List<String> args) {
		Avatar avatar = targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar();
		level = checkLevel(Integer.parseInt(args.get(0)));
		setAvatar(sender, avatar, level);
	}

	public void allLevel(Player sender, Player targetPlayer, List<String> args) {
		level = checkLevel(Integer.parseInt(args.get(1)));

		List<Avatar> avatars = DatabaseHelper.getAvatars(sender);
		for (Avatar avatar : avatars) {
			avatar = sender.getAvatars().getAvatarById(avatar.getAvatarId());
			setAvatar(sender, avatar, level);
		}
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

	public void reloadLevel(Player targetPlayer, List<String> args) {
		try {
			Position targetPlayerPos = targetPlayer.getPosition();
			int scene = targetPlayer.getSceneId();
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, 1, targetPlayerPos);
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, scene, targetPlayerPos);
			targetPlayer.getScene().broadcastPacket(new PacketSceneEntityAppearNotify(targetPlayer));

			switch (args.get(0)) {
				case "all" -> CommandHandler.sendMessage(targetPlayer, "Changed all character levels!");
				case "team" -> CommandHandler.sendMessage(targetPlayer, "Changed team levels!");
				default -> CommandHandler.sendMessage(targetPlayer, "Changed level!");
			} // switch

		} catch (Exception e) {
			CommandHandler.sendMessage(targetPlayer, "Failed to reload! Relog to apply changes.");
		} // catch
	} // reloadLevel
} // setLevelCommand