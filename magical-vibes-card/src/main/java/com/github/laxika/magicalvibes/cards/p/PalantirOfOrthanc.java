package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "HOC", collectorNumber = "45")
@CardRegistration(set = "HOC", collectorNumber = "85")
public class PalantirOfOrthanc extends Card {

    public PalantirOfOrthanc() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.INFLUENCE),
                new ScryEffect(2),
                new MayEffect(
                        new DrawCardEffect(),
                        "Have you draw a card?",
                        new MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect(
                                new CountersOnSource(CounterType.INFLUENCE)),
                        MayChoicePlayer.TARGET_PLAYER)));
    }
}
