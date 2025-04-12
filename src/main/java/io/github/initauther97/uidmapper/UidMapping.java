package io.github.initauther97.uidmapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class UidMapping {

    public static Map<String, UUID> read(Path path, Gson gson) {
        Map<String, UUID> data = Map.of();
        try (final var reader = Files.newBufferedReader(path)) {
            data = gson.fromJson(reader, new TypeToken<>() {});
        } catch (FileNotFoundException | NoSuchFileException e) {
            UUIDMapper.LOGGER.info("Creating UUID mapping file for the first time");
        } catch (IOException e) {
            UUIDMapper.LOGGER.error("Cannot read config file", e);
        }
        return data;
    }

    public static boolean save(Path path, Gson gson, Map<String, UUID> data) {
        try (final var writer = Files.newBufferedWriter(path)) {
            final var raw = gson.toJson(data, new TypeToken<>() {}.getType());
            writer.write(raw);
            writer.flush();
            return true;
        } catch (IOException e) {
            UUIDMapper.LOGGER.error("Cannot save config", e);
            return false;
        }
    }
}
