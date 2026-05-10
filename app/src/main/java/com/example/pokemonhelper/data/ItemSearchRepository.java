package com.example.pokemonhelper.data;

import android.content.Context;

import com.example.pokemonhelper.model.ItemEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ItemSearchRepository {
    private final List<ItemEntry> items;

    public ItemSearchRepository(Context context) {
        this.items = Collections.unmodifiableList(loadItems(context));
    }

    public List<ItemEntry> search(String query, int limit) {
        String normalizedQuery = normalize(query);
        List<ItemEntry> results = new ArrayList<>();
        for (ItemEntry entry : items) {
            if (!normalizedQuery.isEmpty() && !matches(entry, normalizedQuery)) {
                continue;
            }
            results.add(entry);
            if (limit > 0 && results.size() >= limit) {
                break;
            }
        }
        return Collections.unmodifiableList(results);
    }

    private static boolean matches(ItemEntry entry, String query) {
        return normalize(entry.getZhName()).contains(query)
                || normalize(entry.getEnName()).contains(query)
                || normalize(entry.getIdentifier()).contains(query)
                || normalize(entry.getCategory()).contains(query)
                || normalize(entry.getZhEffect()).contains(query)
                || normalize(entry.getShortEffect()).contains(query)
                || String.valueOf(entry.getId()).equals(query);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static List<ItemEntry> loadItems(Context context) {
        List<ItemEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("item_index.tsv"),
                StandardCharsets.UTF_8
        ))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                entries.add(parseLine(line));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load item_index.tsv", exception);
        }
        return entries;
    }

    private static ItemEntry parseLine(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid item tsv row: " + line);
        }
        int cost = parts[5].isEmpty() ? 0 : Integer.parseInt(parts[5]);
        return new ItemEntry(
                Integer.parseInt(parts[0]),
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                cost,
                parts[6],
                parts[7]
        );
    }
}
