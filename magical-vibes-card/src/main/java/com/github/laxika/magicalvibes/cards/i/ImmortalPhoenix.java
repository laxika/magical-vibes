package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "M20", collectorNumber = "332")
public class ImmortalPhoenix extends Card {

    public ImmortalPhoenix() {
        // When this creature dies, return it to its owner's hand.
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
