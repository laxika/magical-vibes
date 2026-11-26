package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "157")
public class LustForWar extends Card {

    public LustForWar() {
        target(TargetFilters.creature());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 3));
        addStaticEffect(new MustAttackEffect());
    }
}
