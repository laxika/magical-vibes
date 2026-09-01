package com.github.laxika.magicalvibes.model.effect;

/** Optional static replacement marker for Jinnie Fay's Cat-or-Dog token choice. */
public record JinnieFayTokenReplacementEffect() implements CardEffect {

    public static final String CAT_OPTION = "Cat";
    public static final String DOG_OPTION = "Dog";
    public static final String ORIGINAL_OPTION = "Original tokens";
}
