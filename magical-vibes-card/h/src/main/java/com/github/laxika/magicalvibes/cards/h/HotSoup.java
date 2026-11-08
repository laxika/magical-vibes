package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "219")
public class HotSoup extends Card {

    public HotSoup() {
        // Equipped creature can't be blocked.
        addStaticEffect(new CantBeBlockedEffect());
        // Whenever equipped creature is dealt damage, destroy it.
        addTrigger(Triggers.enchantedCreatureDealtDamage(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
