package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ALA", collectorNumber = "122")
public class WhereAncientsTread extends Card {

    public WhereAncientsTread() {
        // Whenever a creature you control with power 5 or greater enters, you may have this
        // enchantment deal 5 damage to any target.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(5,
                        new MayEffect(new DealDamageToAnyTargetEffect(5), "Deal 5 damage to any target?")));
    }
}
