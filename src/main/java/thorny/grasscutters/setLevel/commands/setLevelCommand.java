package thorny.grasscutters.setLevel.commands;

import java.util.List;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.Command.TargetRequirement;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketAvatarAddNotify;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;

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

	// Change levels of all characters on the active team
	public void teamLevel(Player sender, Player targetPlayer, List<String> args) {
		level = checkLevel(Integer.parseInt(args.get(1)));

		targetPlayer.getTeamManager().getActiveTeam().forEach(entity -> {
			setAvatar(sender, entity.getAvatar(), level);
		});
	}

	// Change level of current character
	private void activeLevel(Player sender, Player targetPlayer, List<String> args) {
		Avatar avatar = targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar();
		level = checkLevel(Integer.parseInt(args.get(0)));
		setAvatar(sender, avatar, level);
	}

	// Change level of all owned characters
	public void allLevel(Player sender, Player targetPlayer, List<String> args) {
		level = checkLevel(Integer.parseInt(args.get(1)));

		// Iterate through all owned
		sender.getAvatars().forEach(a -> {
			setAvatar(sender, a, level);
		});
	}

	// Validate level is between 1 and 90
	private int checkLevel(int level) {
		level = (level < 1) ? 1 : level;
		level = (level > 90) ? 90 : level;
		return level;
	} // checkLevel

	// Sets level, promotion level, and recalculates stats
	private void setAvatar(Player sender, Avatar avatar, int level) {
		avatar.setPromoteLevel(Avatar.getMinPromoteLevel(level));
		avatar.setLevel(level);
		avatar.recalcStats();
		avatar.save();
		sender.sendPacket(new PacketAvatarAddNotify(avatar, false));
	} // setAvatar

	// Reload the current scene
	public void reloadLevel(Player targetPlayer, List<String> args) {
		try {
			// Transfer back and forth to refresh world without relog
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, 1, targetPlayer.getPosition());
			targetPlayer.getWorld().transferPlayerToScene(targetPlayer, targetPlayer.getSceneId(),
					targetPlayer.getPosition());
			targetPlayer.getScene().broadcastPacket(new PacketSceneEntityAppearNotify(targetPlayer));

			// Send completion message
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