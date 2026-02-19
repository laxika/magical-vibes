package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "132")
public class ContaminatedBond extends Card {

    public ContaminatedBond() {
        setNeedsTarget(true);
        addEffect(EffectSlot.ON_ATTACK, new EnchantedCreatureControllerLosesLifeEffect(3));
        addEffect(EffectSlot.ON_BLOCK, new EnchantedCreatureControllerLosesLifeEffect(3));
    }
}
