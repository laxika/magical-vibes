package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "LCI", collectorNumber = "156")
@CardRegistration(set = "LCI", collectorNumber = "295")
public class IntiSeneschalOfTheSun extends Card {

    public IntiSeneschalOfTheSun() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        SequenceEffect.of(
                                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsAttackingPredicate()),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        "a card"),
                "Discard a card to put a +1/+1 counter on target attacking creature and give it trample?"));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new ExileTopCardsMayPlayUntilNextEndStepEffect(1));
    }
}
