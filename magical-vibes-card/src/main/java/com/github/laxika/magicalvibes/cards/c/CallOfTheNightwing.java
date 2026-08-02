package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "149")
public class CallOfTheNightwing extends Card {

    public CallOfTheNightwing() {
        // Create a 1/1 blue and black Horror creature token with flying.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Horror", 1, 1, null,
                Set.of(CardColor.BLUE, CardColor.BLACK), List.of(CardSubtype.HORROR),
                Set.of(Keyword.FLYING), Set.of()));

        // Cipher
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
