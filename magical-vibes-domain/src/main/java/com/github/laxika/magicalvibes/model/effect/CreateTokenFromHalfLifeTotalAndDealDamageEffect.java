package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Creates an X/X creature token where X is half the controller's life total (rounded up),
 * then the token deals X damage to the controller. Used by Chainer's Torment chapter III.
 *
 * @param tokenName  the name of the token creature
 * @param color      the color of the token
 * @param subtypes   the creature subtypes of the token
 */
public record CreateTokenFromHalfLifeTotalAndDealDamageEffect(
        String tokenName,
        CardColor color,
        List<CardSubtype> subtypes
) implements CardEffect {
}
