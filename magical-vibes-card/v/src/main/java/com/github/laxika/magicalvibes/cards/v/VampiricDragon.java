package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "296")
@CardRegistration(set = "VOC", collectorNumber = "158")
@CardRegistration(set = "ARC", collectorNumber = "99")
public class VampiricDragon extends Card {

    public VampiricDragon() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{1}{R}: This creature deals 1 damage to target creature.",
                TargetFilters.creature()
        ));
    }
}
