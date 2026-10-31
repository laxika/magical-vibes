package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNameExileTopRevealUntilNamedToHandEffect;

@CardRegistration(set = "MRD", collectorNumber = "78")
@CardRegistration(set = "UMA", collectorNumber = "116")
public class SpoilsOfTheVault extends Card {

    public SpoilsOfTheVault() {
        addSpellEffect(new ChooseNameExileTopRevealUntilNamedToHandEffect(0, 1));
    }
}
