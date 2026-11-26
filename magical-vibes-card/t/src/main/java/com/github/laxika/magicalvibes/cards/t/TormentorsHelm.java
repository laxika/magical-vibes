package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "155")
public class TormentorsHelm extends Card {

    public TormentorsHelm() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addTrigger(Triggers.becomesBlocked(),
                DealDamageEffect.to(Subjects.defendingPlayer(), 1).byTriggeringPermanent());
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
