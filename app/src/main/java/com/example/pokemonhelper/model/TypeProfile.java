package com.example.pokemonhelper.model;

import java.util.Collections;
import java.util.List;

public final class TypeProfile {
    private final PokemonType type;
    private final List<PokemonType> strongAgainst;
    private final List<PokemonType> weakAgainstWhenAttacking;
    private final List<PokemonType> noEffectAgainst;
    private final List<PokemonType> weakTo;
    private final List<PokemonType> resists;
    private final List<PokemonType> immuneTo;

    public TypeProfile(
            PokemonType type,
            List<PokemonType> strongAgainst,
            List<PokemonType> weakAgainstWhenAttacking,
            List<PokemonType> noEffectAgainst,
            List<PokemonType> weakTo,
            List<PokemonType> resists,
            List<PokemonType> immuneTo
    ) {
        this.type = type;
        this.strongAgainst = copy(strongAgainst);
        this.weakAgainstWhenAttacking = copy(weakAgainstWhenAttacking);
        this.noEffectAgainst = copy(noEffectAgainst);
        this.weakTo = copy(weakTo);
        this.resists = copy(resists);
        this.immuneTo = copy(immuneTo);
    }

    private static List<PokemonType> copy(List<PokemonType> values) {
        return Collections.unmodifiableList(values);
    }

    public PokemonType getType() {
        return type;
    }

    public List<PokemonType> getStrongAgainst() {
        return strongAgainst;
    }

    public List<PokemonType> getWeakAgainstWhenAttacking() {
        return weakAgainstWhenAttacking;
    }

    public List<PokemonType> getNoEffectAgainst() {
        return noEffectAgainst;
    }

    public List<PokemonType> getWeakTo() {
        return weakTo;
    }

    public List<PokemonType> getResists() {
        return resists;
    }

    public List<PokemonType> getImmuneTo() {
        return immuneTo;
    }
}
