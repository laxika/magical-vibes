package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "236")
public class RetributiveWand extends Card {

    public RetributiveWand() {
        // {3}, {T}: This artifact deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{3}, {T}: This artifact deals 1 damage to any target."));

        // When this artifact is put into a graveyard from the battlefield, it deals 5 damage to any target.
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(5));
    }
}
