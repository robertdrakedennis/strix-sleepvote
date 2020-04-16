package io.github.icohedron.sleepvote;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Supposed to be the lightest possible Voteday.
 */
public class VoteDayCommand implements CommandExecutor {

    private boolean canPlayerVote(Player player) {

        return ! SleepVote.VOTERS.containsKey(player.getUniqueId());

    }

    /**
     * Count's votes.
     */
    private void countVotes(){
        // Get the size of the online players.
        float playersOnline = Sponge.getServer().getOnlinePlayers().size();
        // Get the size of the amount of voters.

        float voters = SleepVote.VOTERS.size();
        // Get the percentage of voters.
        if(voters / playersOnline > 0.5f){
            // Wipe the hashmap.
            SleepVote.VOTERS = new HashMap<>();
            // Get's the target world
            Optional<World> world = Sponge.getServer().getWorld("world");
            // Does it exist?
            world.ifPresent((x) -> {
                WorldProperties properties = x.getProperties();
                long existed = properties.getWorldTime();
                double days = existed / 2400;
                long target = (int)(Math.ceil(days) * 24000);

                x.getProperties().setWorldTime(0);//target + 1800);
                SleepVote.canVote = false;
                broadcastMessage("The vote is complete and there was over 50% of people voting so the time has changed.");
            });

        }

    }

    /**
     * Simply broadcasts to the entire server.
     * @param content
     */
    public static void broadcastMessage(String content){ Sponge.getServer().getOnlinePlayers().forEach((x) -> x.sendMessage(Text.of(content))); }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player){
            Player player = (Player)src;
            if(!SleepVote.canVote){
                player.sendMessage(Text.of("You cannot vote yet!"));

            }else if(canPlayerVote(player)){

                SleepVote.VOTERS.put(player.getUniqueId(), true);
                player.sendMessage(Text.of("Congratulations your vote has been counted."));
                this.countVotes();
            }else{
                player.sendMessage(Text.of("You've already voted, you cannot vote again until the next day/night cycle :("));
            }
        }

        return CommandResult.success();
    }
}
