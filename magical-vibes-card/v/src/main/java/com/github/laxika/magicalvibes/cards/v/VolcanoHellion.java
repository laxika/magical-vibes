package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.ChooseAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "111")
public class VolcanoHellion extends Card {

    public VolcanoHellion() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseAnyNumberEffect())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new DealDamageToTargetCreatureEffect(new ChosenNumberOnSource(), true));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(new ChosenNumberOnSource(), DamageRecipient.CONTROLLER, true));
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect(new ControllerLifeTotal()));
    }
}
