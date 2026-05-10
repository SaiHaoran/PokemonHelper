package com.example.pokemonhelper;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.ComponentActivity;

import com.example.pokemonhelper.ui.PokemonHelperCompose;

public class MainActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        PokemonHelperCompose.attach(this);
    }
}
