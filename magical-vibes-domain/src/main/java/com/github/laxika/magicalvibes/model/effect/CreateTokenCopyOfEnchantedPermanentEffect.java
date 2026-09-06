package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the permanent attached to the source Aura. */
public record CreateTokenCopyOfEnchantedPermanentEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect
) implements CardEffect {

    public CreateTokenCopyOfEnchantedPermanentEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect());
    }

    public static CreateTokenCopyOfEnchantedPermanentEffect exiledAtEndOfCombat() {
        return new CreateTokenCopyOfEnchantedPermanentEffect(
                CreateTokenCopyOfTargetPermanentEffect.exiledAtEndOfCombat());
    }
}
