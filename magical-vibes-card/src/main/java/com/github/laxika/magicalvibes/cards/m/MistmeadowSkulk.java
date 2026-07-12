package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromManaValueEffect;

@CardRegistration(set = "SHM", collectorNumber = "14")
public class MistmeadowSkulk extends Card {

    public MistmeadowSkulk() {
        // Lifelink is auto-loaded from Scryfall. Protection from mana value 3 or greater.
        addEffect(EffectSlot.STATIC, new ProtectionFromManaValueEffect(3));
    }
}
