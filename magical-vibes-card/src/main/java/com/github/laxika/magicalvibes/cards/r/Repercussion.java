package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;

@CardRegistration(set = "UDS", collectorNumber = "95")
public class Repercussion extends Card {

    public Repercussion() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE,
                new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true));
    }
}
