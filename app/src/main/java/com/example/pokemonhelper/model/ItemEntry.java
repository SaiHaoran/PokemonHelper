package com.example.pokemonhelper.model;

public final class ItemEntry {
    private final int id;
    private final String identifier;
    private final String zhName;
    private final String enName;
    private final String category;
    private final int cost;
    private final String shortEffect;
    private final String zhEffect;

    public ItemEntry(
            int id,
            String identifier,
            String zhName,
            String enName,
            String category,
            int cost,
            String shortEffect,
            String zhEffect
    ) {
        this.id = id;
        this.identifier = identifier;
        this.zhName = zhName;
        this.enName = enName;
        this.category = category;
        this.cost = cost;
        this.shortEffect = shortEffect;
        this.zhEffect = zhEffect;
    }

    public int getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getZhName() {
        return zhName;
    }

    public String getEnName() {
        return enName;
    }

    public String getCategory() {
        return category;
    }

    public int getCost() {
        return cost;
    }

    public String getShortEffect() {
        return shortEffect;
    }

    public String getZhEffect() {
        return zhEffect;
    }

    public String getBestEffectText() {
        return zhEffect.isEmpty() ? shortEffect : zhEffect;
    }

    public String getSpriteAssetPath() {
        return "item_sprites/" + identifier + ".png";
    }
}
