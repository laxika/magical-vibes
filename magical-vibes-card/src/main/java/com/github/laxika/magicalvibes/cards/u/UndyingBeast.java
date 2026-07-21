package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

@CardRegistration(set = "POR", collectorNumber = "113")
public class UndyingBeast extends Card {

    public UndyingBeast() {
        // When this creature dies, put it on top of its owner's library.
        addEffect(EffectSlot.ON_DEATH, new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
    }
}
