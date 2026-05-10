package com.example.pokemonhelper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonDefenseProfile {
    private final List<PokemonType> takesFourTimesDamageFrom;
    private final List<PokemonType> takesDoubleDamageFrom;
    private final List<PokemonType> takesHalfDamageFrom;
    private final List<PokemonType> takesQuarterDamageFrom;
    private final List<PokemonType> immuneTo;

    public PokemonDefenseProfile(
            List<PokemonType> takesFourTimesDamageFrom,
            List<PokemonType> takesDoubleDamageFrom,
            List<PokemonType> takesHalfDamageFrom,
            List<PokemonType> takesQuarterDamageFrom,
            List<PokemonType> immuneTo
    ) {
        this.takesFourTimesDamageFrom = copy(takesFourTimesDamageFrom);
        this.takesDoubleDamageFrom = copy(takesDoubleDamageFrom);
        this.takesHalfDamageFrom = copy(takesHalfDamageFrom);
        this.takesQuarterDamageFrom = copy(takesQuarterDamageFrom);
        this.immuneTo = copy(immuneTo);
    }

    private static List<PokemonType> copy(List<PokemonType> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public List<PokemonType> getTakesFourTimesDamageFrom() {
        return takesFourTimesDamageFrom;
    }

    public List<PokemonType> getTakesDoubleDamageFrom() {
        return takesDoubleDamageFrom;
    }

    public List<PokemonType> getTakesHalfDamageFrom() {
        return takesHalfDamageFrom;
    }

    public List<PokemonType> getTakesQuarterDamageFrom() {
        return takesQuarterDamageFrom;
    }

    public List<PokemonType> getImmuneTo() {
        return immuneTo;
    }
}
