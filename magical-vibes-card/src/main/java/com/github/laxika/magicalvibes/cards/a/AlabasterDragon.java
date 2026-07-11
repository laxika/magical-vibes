package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "1")
public class AlabasterDragon extends Card {

    public AlabasterDragon() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_DEATH, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
