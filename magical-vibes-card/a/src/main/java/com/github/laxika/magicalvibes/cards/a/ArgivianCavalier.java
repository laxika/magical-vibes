package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "DMU", collectorNumber = "4")
public class ArgivianCavalier extends Card {

    public ArgivianCavalier() {
        // Enlist is handled by the attack-declaration engine from the Scryfall-loaded keyword.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(1));
    }
}
