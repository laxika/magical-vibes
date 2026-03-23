package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfEachSubtypeFromGraveyardToHandEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "108")
public class GrimCaptainsCall extends Card {

    public GrimCaptainsCall() {
        addEffect(EffectSlot.SPELL, new ReturnOneOfEachSubtypeFromGraveyardToHandEffect(
                List.of(CardSubtype.PIRATE, CardSubtype.VAMPIRE, CardSubtype.DINOSAUR, CardSubtype.MERFOLK)));
    }
}
