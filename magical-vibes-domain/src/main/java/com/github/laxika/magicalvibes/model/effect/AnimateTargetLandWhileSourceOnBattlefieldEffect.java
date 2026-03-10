package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Target land becomes a creature with the given power, toughness, color, and subtypes
 * for as long as the source permanent remains on the battlefield. It's still a land.
 * When the source leaves, the animation is removed.
 *
 * Used by Awakener Druid and similar cards.
 */
public record AnimateTargetLandWhileSourceOnBattlefieldEffect(
        int power, int toughness,
        CardColor color,
        List<CardSubtype> grantedSubtypes
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() { return true; }
}
