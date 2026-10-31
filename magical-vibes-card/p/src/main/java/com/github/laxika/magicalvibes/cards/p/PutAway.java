package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

@CardRegistration(set = "SHM", collectorNumber = "48")
public class PutAway extends Card {

    public PutAway() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect.optionalWhenTargeted(null, 1));
    }
}
