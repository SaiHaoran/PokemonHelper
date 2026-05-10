package com.example.pokemonhelper.model;

public enum PokemonType {
    NORMAL("normal", "一般", "#A8A77A"),
    FIRE("fire", "火", "#EE8130"),
    WATER("water", "水", "#6390F0"),
    ELECTRIC("electric", "电", "#F7D02C"),
    GRASS("grass", "草", "#7AC74C"),
    ICE("ice", "冰", "#96D9D6"),
    FIGHTING("fighting", "格斗", "#C22E28"),
    POISON("poison", "毒", "#A33EA1"),
    GROUND("ground", "地面", "#E2BF65"),
    FLYING("flying", "飞行", "#A98FF3"),
    PSYCHIC("psychic", "超能力", "#F95587"),
    BUG("bug", "虫", "#A6B91A"),
    ROCK("rock", "岩石", "#B6A136"),
    GHOST("ghost", "幽灵", "#735797"),
    DRAGON("dragon", "龙", "#6F35FC"),
    DARK("dark", "恶", "#705746"),
    STEEL("steel", "钢", "#B7B7CE"),
    FAIRY("fairy", "妖精", "#D685AD");

    private final String apiName;
    private final String displayName;
    private final String colorHex;

    PokemonType(String apiName, String displayName, String colorHex) {
        this.apiName = apiName;
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getApiName() {
        return apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static PokemonType fromApiName(String apiName) {
        for (PokemonType type : values()) {
            if (type.apiName.equals(apiName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown Pokemon type api name: " + apiName);
    }
}
