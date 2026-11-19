package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.ChooseAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "111")
public class VolcanoHellion extends Card {

    public VolcanoHellion() {
        var chooseAnyNumber = new ChooseAnyNumberEffect();
        var dealDamageToTargetCreature = new DealDamageToTargetCreatureEffect(new ChosenNumberOnSource(), true);
        target(TargetFilters.creature())
                .bindEffect(chooseAnyNumber)
                .bindEffect(dealDamageToTargetCreature);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                chooseAnyNumber,
                dealDamageToTargetCreature,
                new DealDamageToPlayersEffect(new ChosenNumberOnSource(), Subjects.you(), true));
        addStaticEffect(new EchoEffect(new ControllerLifeTotal()));
    }
}
