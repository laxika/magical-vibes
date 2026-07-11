package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "7")
public class AngelOfFury extends Card {

    public AngelOfFury() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, you may shuffle it into its owner's library."
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                "Shuffle Angel of Fury into its owner's library?"));
    }
}
