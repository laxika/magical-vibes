package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtHandChooseNonlandToBottomAndDrawEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MOR", collectorNumber = "55")
public class VendilionClique extends Card {

    public VendilionClique() {
        // Flash, Flying — auto-loaded from Scryfall.
        //
        // When this creature enters, look at target player's hand. You may choose a nonland card
        // from it. If you do, that player reveals the chosen card, puts it on the bottom of their
        // library, then draws a card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtHandChooseNonlandToBottomAndDrawEffect());
    }
}
