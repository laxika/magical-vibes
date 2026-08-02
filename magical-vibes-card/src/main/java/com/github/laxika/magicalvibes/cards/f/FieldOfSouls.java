package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "TMP", collectorNumber = "18")
public class FieldOfSouls extends Card {

    public FieldOfSouls() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield,
        // create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect.whiteSpirit(1));
    }
}
