package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GTC", collectorNumber = "207")
public class WhisperingMadness extends Card {

    public WhisperingMadness() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect());
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
