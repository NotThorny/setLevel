package thorny.grasscutters.setLevel.commands;

import java.util.List;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;
import emu.grasscutter.utils.Position;


// Command usage
@Command(label = "level", aliases = "lv" , usage = "[level]")
public class setLevelCommand implements CommandHandler {
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
	
	if(!(args.size() < 1)){
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
				try{
					Integer.parseInt(args.get(1));
				}catch(Exception e){
					this.sendUsageMessage(sender);
					break;
				}
				teamLevel(sender, targetPlayer, args);
                break;
            default:
                try {
                    setLevels(sender, targetPlayer, args);
                } catch (NumberFormatException e) {
                    this.sendUsageMessage(sender);
                    throw e;
                }
		}//switch
	}
	else{
		this.sendUsageMessage(sender);
	}
		
	}
	public void allLevel(Player sender, Player targetPlayer, List<String> args) {
		int scene = targetPlayer.getSceneId();

		List<Avatar> avatars = DatabaseHelper.getAvatars(getPlayer(sender, targetPlayer));
		for (Avatar avatar : avatars) {
			int avatarId = avatar.getAvatarId();
			avatar = sender.getAvatars().getAvatarById(avatarId);
			int level = Integer.parseInt(args.get(1));
			int promoteLevel = Avatar.getMinPromoteLevel(level);
			avatar.setPromoteLevel(promoteLevel);
			avatar.setLevel(level);
			avatar.recalcStats();
			avatar.save();
		}
		reloadLevel(targetPlayer, scene, args);
	}

	// Wonky way to get player because I couldn't get an easier way to work
	private Player getPlayer(Player sender, Player targetPlayer) {
		Avatar avatar = sender.getAvatars().getAvatarById(targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar().getAvatarId());
		Player player = avatar.getPlayer();
		return player;
	}
	public void teamLevel(Player sender, Player targetPlayer, List<String> args) {
		int scene = targetPlayer.getSceneId();

		targetPlayer.getTeamManager().getActiveTeam().forEach(entity -> {
			int avatarId = entity.getAvatar().getAvatarId();
			Avatar avatar = sender.getAvatars().getAvatarById(avatarId);
			int level = Integer.parseInt(args.get(1));
			int promoteLevel = Avatar.getMinPromoteLevel(level);
			avatar.setPromoteLevel(promoteLevel);
			avatar.setLevel(level);
			avatar.recalcStats();
			avatar.save();
        });

		reloadLevel(targetPlayer, scene, args);
	}
	

	private void setLevels(Player sender, Player targetPlayer, List<String> args) {
		int pId = targetPlayer.getTeamManager().getCurrentAvatarEntity().getAvatar().getAvatarId();
		Avatar avatar = sender.getAvatars().getAvatarById(pId);
		int scene = targetPlayer.getSceneId();
		int level = Integer.parseInt(args.get(0));
		if (level < 1) level = 1;
        if (level > 90) level = 90;
		int promoteLevel = Avatar.getMinPromoteLevel(level);
		avatar.setPromoteLevel(promoteLevel);
		avatar.setLevel(level);
		avatar.recalcStats();
		avatar.save();
		
		reloadLevel(targetPlayer, scene, args);
	}

	public void reloadLevel(Player targetPlayer, int scene, List<String> args) {
	try {
		Position targetPlayerPos = targetPlayer.getPosition();
		targetPlayer.getWorld().transferPlayerToScene(targetPlayer, 1, targetPlayerPos);
		targetPlayer.getWorld().transferPlayerToScene(targetPlayer, scene, targetPlayerPos);
		targetPlayer.getScene().broadcastPacket(new PacketSceneEntityAppearNotify(targetPlayer));

		switch (args.get(0)) {
			case "all":
				CommandHandler.sendMessage(targetPlayer, "Changed all character levels!"+
					"\n*YOU MUST RELOG FOR ALL CHARACTER LEVELS TO APPLY*");
                break;
            case "team":
				CommandHandler.sendMessage(targetPlayer, "Changed team levels!");
                break;
            default:
				CommandHandler.sendMessage(targetPlayer, "Changed level!");
                
		}//switch
	} catch (Exception e) {
		CommandHandler.sendMessage(targetPlayer, "Failed to reload! Relog to apply changes.");
	}
	}
}
