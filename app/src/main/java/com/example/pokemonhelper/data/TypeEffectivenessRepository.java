package com.example.pokemonhelper.data;

import com.example.pokemonhelper.model.PokemonType;
import com.example.pokemonhelper.model.TypeProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TypeEffectivenessRepository {
    private static final Map<PokemonType, TypeMatchup> MATCHUPS = buildMatchups();
    private static final Map<PokemonType, TypeProfile> PROFILES = buildProfiles();
    private static final List<PokemonType> ALL_TYPES =
            Collections.unmodifiableList(Arrays.asList(PokemonType.values()));

    private TypeEffectivenessRepository() {
    }

    public static List<PokemonType> getAllTypes() {
        return ALL_TYPES;
    }

    public static TypeProfile getProfile(PokemonType type) {
        TypeProfile profile = PROFILES.get(type);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown Pokemon type: " + type);
        }
        return profile;
    }

    private static Map<PokemonType, TypeProfile> buildProfiles() {
        Map<PokemonType, TypeProfile> profiles = new EnumMap<>(PokemonType.class);
        for (PokemonType type : PokemonType.values()) {
            TypeMatchup attack = MATCHUPS.get(type);
            profiles.put(type, new TypeProfile(
                    type,
                    attack.doubleDamageTo,
                    attack.halfDamageTo,
                    attack.noDamageTo,
                    findAttackersThatDoubleDamage(type),
                    findAttackersThatHalfDamage(type),
                    findAttackersThatCannotDamage(type)
            ));
        }
        return Collections.unmodifiableMap(profiles);
    }

    private static List<PokemonType> findAttackersThatDoubleDamage(PokemonType defender) {
        return findAttackers(defender, Relation.DOUBLE);
    }

    private static List<PokemonType> findAttackersThatHalfDamage(PokemonType defender) {
        return findAttackers(defender, Relation.HALF);
    }

    private static List<PokemonType> findAttackersThatCannotDamage(PokemonType defender) {
        return findAttackers(defender, Relation.NONE);
    }

    private static List<PokemonType> findAttackers(PokemonType defender, Relation relation) {
        List<PokemonType> attackers = new ArrayList<>();
        for (PokemonType attacker : PokemonType.values()) {
            TypeMatchup matchup = MATCHUPS.get(attacker);
            List<PokemonType> targets;
            switch (relation) {
                case DOUBLE:
                    targets = matchup.doubleDamageTo;
                    break;
                case HALF:
                    targets = matchup.halfDamageTo;
                    break;
                case NONE:
                    targets = matchup.noDamageTo;
                    break;
                default:
                    throw new IllegalStateException("Unexpected relation: " + relation);
            }
            if (targets.contains(defender)) {
                attackers.add(attacker);
            }
        }
        return Collections.unmodifiableList(attackers);
    }

    private static Map<PokemonType, TypeMatchup> buildMatchups() {
        Map<PokemonType, TypeMatchup> map = new EnumMap<>(PokemonType.class);
        map.put(PokemonType.NORMAL, matchup(
                types(),
                types(PokemonType.ROCK, PokemonType.STEEL),
                types(PokemonType.GHOST)
        ));
        map.put(PokemonType.FIRE, matchup(
                types(PokemonType.GRASS, PokemonType.ICE, PokemonType.BUG, PokemonType.STEEL),
                types(PokemonType.FIRE, PokemonType.WATER, PokemonType.ROCK, PokemonType.DRAGON),
                types()
        ));
        map.put(PokemonType.WATER, matchup(
                types(PokemonType.FIRE, PokemonType.GROUND, PokemonType.ROCK),
                types(PokemonType.WATER, PokemonType.GRASS, PokemonType.DRAGON),
                types()
        ));
        map.put(PokemonType.ELECTRIC, matchup(
                types(PokemonType.WATER, PokemonType.FLYING),
                types(PokemonType.ELECTRIC, PokemonType.GRASS, PokemonType.DRAGON),
                types(PokemonType.GROUND)
        ));
        map.put(PokemonType.GRASS, matchup(
                types(PokemonType.WATER, PokemonType.GROUND, PokemonType.ROCK),
                types(PokemonType.FIRE, PokemonType.GRASS, PokemonType.POISON, PokemonType.FLYING, PokemonType.BUG, PokemonType.DRAGON, PokemonType.STEEL),
                types()
        ));
        map.put(PokemonType.ICE, matchup(
                types(PokemonType.GRASS, PokemonType.GROUND, PokemonType.FLYING, PokemonType.DRAGON),
                types(PokemonType.FIRE, PokemonType.WATER, PokemonType.ICE, PokemonType.STEEL),
                types()
        ));
        map.put(PokemonType.FIGHTING, matchup(
                types(PokemonType.NORMAL, PokemonType.ICE, PokemonType.ROCK, PokemonType.DARK, PokemonType.STEEL),
                types(PokemonType.POISON, PokemonType.FLYING, PokemonType.PSYCHIC, PokemonType.BUG, PokemonType.FAIRY),
                types(PokemonType.GHOST)
        ));
        map.put(PokemonType.POISON, matchup(
                types(PokemonType.GRASS, PokemonType.FAIRY),
                types(PokemonType.POISON, PokemonType.GROUND, PokemonType.ROCK, PokemonType.GHOST),
                types(PokemonType.STEEL)
        ));
        map.put(PokemonType.GROUND, matchup(
                types(PokemonType.FIRE, PokemonType.ELECTRIC, PokemonType.POISON, PokemonType.ROCK, PokemonType.STEEL),
                types(PokemonType.GRASS, PokemonType.BUG),
                types(PokemonType.FLYING)
        ));
        map.put(PokemonType.FLYING, matchup(
                types(PokemonType.GRASS, PokemonType.FIGHTING, PokemonType.BUG),
                types(PokemonType.ELECTRIC, PokemonType.ROCK, PokemonType.STEEL),
                types()
        ));
        map.put(PokemonType.PSYCHIC, matchup(
                types(PokemonType.FIGHTING, PokemonType.POISON),
                types(PokemonType.PSYCHIC, PokemonType.STEEL),
                types(PokemonType.DARK)
        ));
        map.put(PokemonType.BUG, matchup(
                types(PokemonType.GRASS, PokemonType.PSYCHIC, PokemonType.DARK),
                types(PokemonType.FIRE, PokemonType.FIGHTING, PokemonType.POISON, PokemonType.FLYING, PokemonType.GHOST, PokemonType.STEEL, PokemonType.FAIRY),
                types()
        ));
        map.put(PokemonType.ROCK, matchup(
                types(PokemonType.FIRE, PokemonType.ICE, PokemonType.FLYING, PokemonType.BUG),
                types(PokemonType.FIGHTING, PokemonType.GROUND, PokemonType.STEEL),
                types()
        ));
        map.put(PokemonType.GHOST, matchup(
                types(PokemonType.PSYCHIC, PokemonType.GHOST),
                types(PokemonType.DARK),
                types(PokemonType.NORMAL)
        ));
        map.put(PokemonType.DRAGON, matchup(
                types(PokemonType.DRAGON),
                types(PokemonType.STEEL),
                types(PokemonType.FAIRY)
        ));
        map.put(PokemonType.DARK, matchup(
                types(PokemonType.PSYCHIC, PokemonType.GHOST),
                types(PokemonType.FIGHTING, PokemonType.DARK, PokemonType.FAIRY),
                types()
        ));
        map.put(PokemonType.STEEL, matchup(
                types(PokemonType.ICE, PokemonType.ROCK, PokemonType.FAIRY),
                types(PokemonType.FIRE, PokemonType.WATER, PokemonType.ELECTRIC, PokemonType.STEEL),
                types()
        ));
        map.put(PokemonType.FAIRY, matchup(
                types(PokemonType.FIGHTING, PokemonType.DRAGON, PokemonType.DARK),
                types(PokemonType.FIRE, PokemonType.POISON, PokemonType.STEEL),
                types()
        ));
        return Collections.unmodifiableMap(map);
    }

    private static TypeMatchup matchup(
            List<PokemonType> doubleDamageTo,
            List<PokemonType> halfDamageTo,
            List<PokemonType> noDamageTo
    ) {
        return new TypeMatchup(doubleDamageTo, halfDamageTo, noDamageTo);
    }

    private static List<PokemonType> types(PokemonType... types) {
        return Collections.unmodifiableList(Arrays.asList(types));
    }

    private enum Relation {
        DOUBLE,
        HALF,
        NONE
    }

    private static final class TypeMatchup {
        private final List<PokemonType> doubleDamageTo;
        private final List<PokemonType> halfDamageTo;
        private final List<PokemonType> noDamageTo;

        private TypeMatchup(
                List<PokemonType> doubleDamageTo,
                List<PokemonType> halfDamageTo,
                List<PokemonType> noDamageTo
        ) {
            this.doubleDamageTo = doubleDamageTo;
            this.halfDamageTo = halfDamageTo;
            this.noDamageTo = noDamageTo;
        }
    }
}
