package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "68")
@CardRegistration(set = "9ED", collectorNumber = "61")
@CardRegistration(set = "8ED", collectorNumber = "61")
public class AvenFisher extends Card {

    public AvenFisher() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
