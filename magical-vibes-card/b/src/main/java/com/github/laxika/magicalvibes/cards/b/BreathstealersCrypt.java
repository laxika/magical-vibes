package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BreathstealersCryptDrawReplacementEffect;

@CardRegistration(set = "VIS", collectorNumber = "127")
public class BreathstealersCrypt extends Card {

    public BreathstealersCrypt() {
        // If a player would draw a card, instead they draw a card and reveal it. If it's a creature
        // card, that player discards it unless they pay 3 life. Detected in DrawService.
        addEffect(EffectSlot.STATIC, new BreathstealersCryptDrawReplacementEffect());
    }
}
