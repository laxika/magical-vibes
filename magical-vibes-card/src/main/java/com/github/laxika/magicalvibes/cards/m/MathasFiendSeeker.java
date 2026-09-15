package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "252")
public class MathasFiendSeeker extends Card {

    public MathasFiendSeeker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantEffectsToCounterBearersEffect(CounterType.BOUNTY, List.of(
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_DEATH,
                                SequenceEffect.of(
                                        new EachOpponentDrawsCardEffect(1),
                                        new GainLifeEffect(new Fixed(2), GainLifeRecipient.OPPONENT)),
                                GrantScope.SELF))));

        target(TargetFilters.creatureAnOpponentControls()).addEffect(
                EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCounterOnTargetPermanentEffect(CounterType.BOUNTY));
    }
}
