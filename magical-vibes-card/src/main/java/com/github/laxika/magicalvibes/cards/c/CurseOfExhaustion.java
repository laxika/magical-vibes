package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsForEnchantedPlayerEffect;

@CardRegistration(set = "DKA", collectorNumber = "5")
public class CurseOfExhaustion extends Card {

    public CurseOfExhaustion() {
        // Enchanted player can't cast more than one spell each turn.
        addEffect(EffectSlot.STATIC, new LimitSpellsForEnchantedPlayerEffect(1));
    }
}
