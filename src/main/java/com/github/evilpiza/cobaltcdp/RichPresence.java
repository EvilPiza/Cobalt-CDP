package com.github.evilpiza.cobaltcdp;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mod(modid = "cobaltcdp", name = "Cobalt CDP", version = "1.0.0")
public class RichPresence {
    private static String APPLICATION_ID = "1364787314592714782";
    private static String DLL_PATH = "/natives/discord_game_sdk.dll";
    
    private DiscordRPC lib;
    private DiscordRichPresence presence;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            // Load native library
            InputStream dllStream = getClass().getResourceAsStream(DLL_PATH);
            if (dllStream == null) {
                throw new RuntimeException("Failed to find Discord SDK DLL in resources");
            }
            
            File tempFile = File.createTempFile("discord_game_sdk", ".dll");
            Files.copy(dllStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.deleteOnExit();
            System.load(tempFile.getAbsolutePath());

            // Initialize RPC
            lib = DiscordRPC.INSTANCE;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            lib.Discord_Initialize(APPLICATION_ID, handlers, true, null);

            presence = new DiscordRichPresence();
            presence.largeImageKey = "minecraft";
            presence.largeImageText = "Cobalt CDP";
            lib.Discord_UpdatePresence(presence);

            MinecraftForge.EVENT_BUS.register(this);

            // Callback thread
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    lib.Discord_RunCallbacks();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        lib.Discord_Shutdown();
                        break;
                    }
                }
            }, "RPC-Callback").start();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Discord RPC", e);
        }
    }

    public void SetPresence(String state) {
        presence.state = state;
        lib.Discord_UpdatePresence(presence);
    }

    public void SetPresence(String details, String state) {
        presence.details = details;
        presence.state = state;
        lib.Discord_UpdatePresence(presence);
    }

    public void SetDiscordAppID(String id) {
        APPLICATION_ID = id;
    }

    public void SetDLLPath(String path) {
        DLL_PATH = path;
    }

    @Mod.EventHandler
    public void onShutdown(net.minecraftforge.fml.common.event.FMLServerStoppingEvent event) {
        lib.Discord_Shutdown();
    }
}