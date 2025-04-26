package com.github.evilpiza.cobaltcdp;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RichPresence {
    private static String APPLICATION_ID = " APP ID HERE ";
    private static final String DLL_PATH = "/natives/discord_game_sdk.dll";
    
    private DiscordRPC lib;
    private DiscordRichPresence presence;

    public void init() {
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
}
