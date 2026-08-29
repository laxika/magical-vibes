package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ATH", collectorNumber = "23")
@CardRegistration(set = "FEM", collectorNumber = "38a")
@CardRegistration(set = "FEM", collectorNumber = "38b")
@CardRegistration(set = "FEM", collectorNumber = "38c")
@CardRegistration(set = "FEM", collectorNumber = "38d")
public class HymnToTourach extends Card {

    public HymnToTourach() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL,
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, true));
    }
}
