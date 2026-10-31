package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

@CardRegistration(set = "GRN", collectorNumber = "35")
@CardRegistration(set = "MID", collectorNumber = "48")
@CardRegistration(set = "DBL", collectorNumber = "48")
public class DeviousCoverUp extends Card {

    public DeviousCoverUp() {
        addSpellEffect(new CounterSpellEffect(CounteredSpellDestination.EXILE));
        addSpellEffect(ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect.optional(null, 4));
    }
}
