package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "34")
@CardRegistration(set = "OPCA", collectorNumber = "74")
@CardRegistration(set = "MOC", collectorNumber = "159")
public class Stensia extends Card {

    public Stensia() {
        var putCounterOnDamagingCreature = new PutCountersEffect(
                Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE);
        addTrigger(List.of(
                        Triggers.sourceYouControlDealsDamageToOpponent(),
                        Triggers.permanentDealsDamageToYou(),
                        Triggers.creatureDealsDamageToItsController()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE),
                putCounterOnDamagingCreature);

        addTrigger(Triggers.chaosEnsues(), new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(DealDamageEffect.toTargetPlayerOrPlaneswalker(1)),
                        "{T}: This creature deals 1 damage to target player or planeswalker."),
                Subjects.otherCreaturesYouControl(),
                null,
                Duration.UNTIL_END_OF_TURN));
    }
}
