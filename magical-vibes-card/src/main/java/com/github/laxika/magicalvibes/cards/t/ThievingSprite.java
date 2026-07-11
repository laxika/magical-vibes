package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "143")
public class ThievingSprite extends Card {

    public ThievingSprite() {
        // Flying — auto-loaded from Scryfall.
        //
        // When this creature enters, target player reveals X cards from their hand, where X is the
        // number of Faeries you control (Thieving Sprite counts itself). You choose one of those
        // cards; that player discards it.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealCardsChooseOneToDiscardEffect(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE))));
    }
}
