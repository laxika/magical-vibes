package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "252")
@CardRegistration(set = "C17", collectorNumber = "42")
public class MathasFiendSeeker extends Card {

    public MathasFiendSeeker() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.BOUNTY),
                        new GrantEffectsToCounterBearersEffect(CounterType.BOUNTY, List.of(
                                new GrantTriggeredAbilityEffect(
                                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                                        SequenceEffect.of(
                                                new EachOpponentDrawsCardEffect(1),
                                                new GainLifeEffect(new Fixed(2), GainLifeRecipient.OPPONENT))),
                                        GrantScope.SELF)),
                                true)));
    }
}
