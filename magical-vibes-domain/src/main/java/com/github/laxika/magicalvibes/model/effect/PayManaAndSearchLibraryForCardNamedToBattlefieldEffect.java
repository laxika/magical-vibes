package com.github.laxika.magicalvibes.model.effect;

public record PayManaAndSearchLibraryForCardNamedToBattlefieldEffect(
        String manaCost,
        String cardName
) implements CardEffect {
}
