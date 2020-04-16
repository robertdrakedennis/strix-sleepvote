package io.github.icohedron.sleepvote;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "sleepvote", name = "SleepVote", version = "1.0",
        description = "A light Vote day.",
        dependencies = @Dependency(id = "nucleus", optional = true))
public class SleepVote {

    private static final String SETTINGS_CONFIG_NAME = "configuration.properties";

    public static HashMap<UUID, Boolean> VOTERS = new HashMap<>();

    public static boolean canVote = false;
    private static PluginContainer plugin;
    @Inject @ConfigDir(sharedRoot = false)
    private Path configurationDirectory;

    @Inject
    private Logger logger;

    //private SleepVoteManager sleepVoteManager;

    private void doTickChecks() {

        Optional<World> world = Sponge.getServer().getWorld("world");

        world.ifPresent( (x) ->{
            long time = x.getProperties().getWorldTime() % 24000;
            //VoteDayCommand.broadcastMessage("TIME: " + String.valueOf(time));
            if(time > 12560 && !canVote && time < 20000){
                canVote = true;
                VoteDayCommand.broadcastMessage(" The vote is now open (do /voteday for day time)!");
                return;
            }
            if(time > 20000 && canVote){
                VoteDayCommand.broadcastMessage(" The vote is now closed and the majority didn't vote :(");
                VOTERS = new HashMap<>();
                canVote = false;
                return;
            }



        });

    }
    @Listener
    public void onPreInitializationEvent(GamePreInitializationEvent event) {
        plugin = Sponge.getPluginManager().getPlugin("sleepvote").get();

    }
    @Listener
    public void onInitializationEvent(GameInitializationEvent event) {
        logger.info("Finished initialization");
        Task.builder().async().delay(250, TimeUnit.MILLISECONDS).interval(1, TimeUnit.MILLISECONDS).execute( () -> doTickChecks()).submit(plugin);
        CommandSpec spec = CommandSpec.builder()
                .description(Text.of("Simply votes day."))
                .executor(new VoteDayCommand())
                .build();
        Sponge.getCommandManager().register(plugin, spec, "voteday");
        Sponge.getServiceManager().provide(PermissionService.class);
    }

    @Listener
    public void onReloadEvent(GameReloadEvent event) {
        reload();
    }

    private void reload() {
        //sleepVoteManager.dispose();
        //Sponge.getEventManager().unregisterListeners(sleepVoteManager);
        //loadConfiguration();
    }

    Logger getLogger() {
        return logger;
    }
}
