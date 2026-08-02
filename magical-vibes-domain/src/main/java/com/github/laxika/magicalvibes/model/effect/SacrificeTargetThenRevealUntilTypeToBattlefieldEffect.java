package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Sacrifices the targeted permanent, then reveals cards from the top of its controller's library
 * until a card matching one of the specified types is found. That card is put onto the battlefield
 * under that player's control, and all other revealed cards are shuffled into their library.
 * <p>
 * Used by Shape Anew (artifact → artifact), and can be reused for Polymorph-style effects
 * (creature → creature) by changing the cardTypes parameter.
 * <p>
 * An <em>empty</em> {@code cardTypes} set switches to Reweave's dynamic mode: the revealed card must
 * be a permanent card that shares a card type with the sacrificed permanent, rather than matching a
 * fixed set of types.
 */
public record SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(
        Set<CardType> cardTypes
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
