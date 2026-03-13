package com.github.laxika.magicalvibes.model.effect;

public record SearchLibraryForCardsByNameToHandEffect(
        String cardName,
        int maxCount
) implements CardEffect {
}
