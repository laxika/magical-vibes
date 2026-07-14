package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawDrawOnBasicLandEffect;

@CardRegistration(set = "7ED", collectorNumber = "266")
@CardRegistration(set = "6ED", collectorNumber = "250")
public class Rowen extends Card {

    public Rowen() {
        // "Reveal the first card you draw each turn. Whenever you reveal a basic land card this way,
        // draw a card." Both the reveal and the draw trigger are handled in DrawService.
        addEffect(EffectSlot.STATIC, new RevealFirstDrawDrawOnBasicLandEffect());
    }
}
