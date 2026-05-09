package com.example.pokemonhelper.data;

import android.content.Context;

import com.example.pokemonhelper.model.PokemonEntry;
import com.example.pokemonhelper.model.PokemonType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PokemonSearchRepository {
    private final List<PokemonEntry> pokemon;

    public PokemonSearchRepository(Context context) {
        this.pokemon = Collections.unmodifiableList(loadPokemon(context));
    }

    public List<PokemonEntry> search(String query, PokemonType typeFilter, int limit) {
        String normalizedQuery = normalize(query);
        List<PokemonEntry> results = new ArrayList<>();
        for (PokemonEntry entry : pokemon) {
            if (typeFilter != null && !entry.getTypes().contains(typeFilter)) {
                continue;
            }
            if (!normalizedQuery.isEmpty() && !matches(entry, normalizedQuery)) {
                continue;
            }
            results.add(entry);
            if (results.size() >= limit) {
                break;
            }
        }
        return Collections.unmodifiableList(results);
    }

    public List<PokemonEntry> getAllPokemon() {
        return pokemon;
    }

    private static boolean matches(PokemonEntry entry, String query) {
        return normalize(entry.getZhName()).contains(query)
                || normalize(entry.getEnName()).contains(query)
                || normalize(entry.getApiName()).contains(query)
                || String.valueOf(entry.getId()).equals(query);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static List<PokemonEntry> loadPokemon(Context context) {
        List<PokemonEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("pokemon_index.csv"),
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
            throw new IllegalStateException("Unable to load pokemon_index.csv", exception);
        }
        return entries;
    }

    private static PokemonEntry parseLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid pokemon csv row: " + line);
        }
        String[] typeNames = parts[4].split("\\|", -1);
        List<PokemonType> types = new ArrayList<>();
        for (String typeName : typeNames) {
            types.add(PokemonType.fromApiName(typeName));
        }
        return new PokemonEntry(
                Integer.parseInt(parts[0]),
                parts[1],
                parts[2],
                parts[3],
                types
        );
    }
}
