package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DGM", collectorNumber = "18")
public class TraitDoctoring extends Card {

    public TraitDoctoring() {
        // Change the text of target permanent by replacing all instances of one color word with
        // another or one basic land type with another until end of turn.
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, true, false, true));
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
