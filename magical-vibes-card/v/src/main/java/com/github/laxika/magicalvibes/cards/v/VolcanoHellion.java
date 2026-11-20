package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.ChooseAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "111")
public class VolcanoHellion extends Card {

    public VolcanoHellion() {
        var chooseAnyNumber = new ChooseAnyNumberEffect();
        var dealDamageToTargetCreature = new DealDamageToTargetCreatureEffect(new ChosenNumberOnSource(), true);
        target(TargetFilters.creature())
                .bindEffect(chooseAnyNumber)
                .bindEffect(dealDamageToTargetCreature);
        addTriggeredAbility(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), TriggerOptions.DEFAULT, List.of(
                new EffectRegistration(chooseAnyNumber),
                new EffectRegistration(dealDamageToTargetCreature),
                new EffectRegistration(new DealDamageToPlayersEffect(new ChosenNumberOnSource(), Subjects.you(), true),
                        true))));
        addStaticEffect(new EchoEffect(new ControllerLifeTotal()));
    }
}
