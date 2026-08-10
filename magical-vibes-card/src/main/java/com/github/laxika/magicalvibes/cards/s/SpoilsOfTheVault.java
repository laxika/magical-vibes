package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNameExileTopRevealUntilNamedToHandEffect;

@CardRegistration(set = "MRD", collectorNumber = "78")
public class SpoilsOfTheVault extends Card {

    public SpoilsOfTheVault() {
        addEffect(EffectSlot.SPELL, new ChooseNameExileTopRevealUntilNamedToHandEffect(0, 1));
    }
}
