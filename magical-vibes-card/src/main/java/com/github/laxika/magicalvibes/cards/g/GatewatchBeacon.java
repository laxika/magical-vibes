package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "CMM", collectorNumber = "721")
@CardRegistration(set = "CMM", collectorNumber = "754")
public class GatewatchBeacon extends Card {

    public GatewatchBeacon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.LOYALTY, new Fixed(3)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsPlaneswalkerPredicate(),
                        new MayEffect(
                                new ConditionalEffect(
                                        new SourceCounterThreshold(1, CounterType.LOYALTY),
                                        new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.LOYALTY)),
                                "Move a loyalty counter onto that planeswalker?")));
    }
}
