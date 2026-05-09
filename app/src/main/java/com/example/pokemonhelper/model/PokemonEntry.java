package com.example.pokemonhelper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonEntry {
    private final int id;
    private final String apiName;
    private final String zhName;
    private final String enName;
    private final List<PokemonType> types;

    public PokemonEntry(
            int id,
            String apiName,
            String zhName,
            String enName,
            List<PokemonType> types
    ) {
        this.id = id;
        this.apiName = apiName;
        this.zhName = zhName;
        this.enName = enName;
        this.types = Collections.unmodifiableList(new ArrayList<>(types));
    }

    public int getId() {
        return id;
    }

    public String getApiName() {
        return apiName;
    }

    public String getZhName() {
        return zhName;
    }

    public String getEnName() {
        return enName;
    }

    public List<PokemonType> getTypes() {
        return types;
    }
}
