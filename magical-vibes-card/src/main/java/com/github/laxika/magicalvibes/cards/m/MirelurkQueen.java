package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PIP", collectorNumber = "33")
@CardRegistration(set = "PIP", collectorNumber = "561")
@CardRegistration(set = "PIP", collectorNumber = "377")
@CardRegistration(set = "PIP", collectorNumber = "905")
public class MirelurkQueen extends Card {

    public MirelurkQueen() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GiveTargetPlayerRadCountersEffect(2));

        addEffect(EffectSlot.ON_NONLAND_CARDS_MILLED,
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new DrawCardEffect(1),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
