package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TSP", collectorNumber = "112")
public class HauntingHymn extends Card {

    public HauntingHymn() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastDuringMainPhase(), new DiscardEffect(4, DiscardRecipient.TARGET_PLAYER)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastDuringMainPhase()),
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)));
    }
}
