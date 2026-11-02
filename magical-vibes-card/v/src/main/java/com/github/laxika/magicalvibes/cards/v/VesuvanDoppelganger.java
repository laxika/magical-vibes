package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SUM", collectorNumber = "88")
@CardRegistration(set = "ME1", collectorNumber = "54")
@CardRegistration(set = "3ED", collectorNumber = "88")
@CardRegistration(set = "2ED", collectorNumber = "88")
@CardRegistration(set = "LEA", collectorNumber = "87")
@CardRegistration(set = "LEB", collectorNumber = "88")
public class VesuvanDoppelganger extends Card {

    public VesuvanDoppelganger() {
        addStaticEffect(new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                List.of(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), 
                        new BecomeCopyOfTargetCreatureEffect(null, false, true))),
                false
        ));
    }
}
