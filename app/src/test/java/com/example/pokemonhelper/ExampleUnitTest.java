package com.example.pokemonhelper;

import com.example.pokemonhelper.data.TypeEffectivenessRepository;
import com.example.pokemonhelper.model.PokemonType;
import com.example.pokemonhelper.model.TypeProfile;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void fireType_hasExpectedAttackRelations() {
        TypeProfile fire = TypeEffectivenessRepository.getProfile(PokemonType.FIRE);

        assertTrue(fire.getStrongAgainst().contains(PokemonType.GRASS));
        assertTrue(fire.getStrongAgainst().contains(PokemonType.STEEL));
        assertTrue(fire.getWeakAgainstWhenAttacking().contains(PokemonType.WATER));
        assertFalse(fire.getStrongAgainst().contains(PokemonType.WATER));
    }

    @Test
    public void electricAndGround_immunityIsModeledBothWays() {
        TypeProfile electric = TypeEffectivenessRepository.getProfile(PokemonType.ELECTRIC);
        TypeProfile ground = TypeEffectivenessRepository.getProfile(PokemonType.GROUND);

        assertTrue(electric.getNoEffectAgainst().contains(PokemonType.GROUND));
        assertTrue(ground.getImmuneTo().contains(PokemonType.ELECTRIC));
    }

    @Test
    public void allTypesAreAvailable() {
        assertEquals(18, TypeEffectivenessRepository.getAllTypes().size());
    }
}
