package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "127")
public class ReleaseTheDogs extends Card {

    public ReleaseTheDogs() {
        // Create four 1/1 white Dog creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Dog", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.DOG), Set.of(), Set.of()));
    }
}
