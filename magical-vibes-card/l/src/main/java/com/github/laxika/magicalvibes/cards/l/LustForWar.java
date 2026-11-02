package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "157")
public class LustForWar extends Card {

    public LustForWar() {
        target(TargetFilters.creature());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new DealDamageToPlayersEffect(3, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
        addStaticEffect(new MustAttackEffect());
    }
}
