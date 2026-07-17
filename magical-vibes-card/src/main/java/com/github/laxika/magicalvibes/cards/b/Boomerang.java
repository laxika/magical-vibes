package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "75")
@CardRegistration(set = "10E", collectorNumber = "70")
@CardRegistration(set = "9ED", collectorNumber = "66")
@CardRegistration(set = "8ED", collectorNumber = "63")
@CardRegistration(set = "7ED", collectorNumber = "64")
@CardRegistration(set = "6ED", collectorNumber = "58")
public class Boomerang extends Card {

    public Boomerang() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
