package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "DKA", collectorNumber = "6")
public class ElgaudInquisitor extends Card {

    public ElgaudInquisitor() {
        // Lifelink is loaded automatically from Scryfall.
        // When Elgaud Inquisitor dies, create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.whiteSpirit(1));
    }
}
