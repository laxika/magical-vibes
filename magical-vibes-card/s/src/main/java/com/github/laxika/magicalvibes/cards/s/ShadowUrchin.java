package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "ECL", collectorNumber = "242")
@CardRegistration(set = "ECL", collectorNumber = "379")
public class ShadowUrchin extends Card {

    public ShadowUrchin() {
        addEffect(EffectSlot.ON_ATTACK, new BlightEffect(1, null));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.ANY),
                new ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()));
    }
}
