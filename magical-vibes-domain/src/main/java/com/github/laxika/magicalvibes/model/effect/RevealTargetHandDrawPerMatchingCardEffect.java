package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * The target opponent reveals their hand, then the caster draws one card for each card in it that
 * has any of the given {@code subtypes} or any of the given {@code colors} (each matching card is
 * counted once, even if it matches on both a subtype and a color). Used by Baleful Stare
 * (subtypes = MOUNTAIN, colors = RED).
 */
public record RevealTargetHandDrawPerMatchingCardEffect(List<CardSubtype> subtypes,
                                                        List<CardColor> colors) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
