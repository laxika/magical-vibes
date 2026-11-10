package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "113")
public class MagneticMine extends Card {

    public MagneticMine() {
        addTrigger(Triggers.dies(new PermanentAllOfPredicate(List.of(Triggers.ARTIFACT,
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))), ObjectRelation.ANY),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
