package com.github.laxika.magicalvibes.model.effect;

/** Resolves The Master's optional exile and villainous choice for a dying artifact creature. */
public record TheMasterGallifreysEndEffect() implements CardEffect {

    public static final String LOSE_LIFE_OPTION = "They lose 4 life";
    public static final String CREATE_TOKEN_OPTION = "You create a token that's a copy of that card";
}
