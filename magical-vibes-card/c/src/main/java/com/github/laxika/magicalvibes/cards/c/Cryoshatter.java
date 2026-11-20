package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "53")
public class Cryoshatter extends Card {

    public Cryoshatter() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-5, 0, Subjects.attachedCreature()))
                .addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                        new DestroyTargetPermanentEffect(Subjects.triggering()))
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new DestroyTargetPermanentEffect(Subjects.triggering()));
    }
}
