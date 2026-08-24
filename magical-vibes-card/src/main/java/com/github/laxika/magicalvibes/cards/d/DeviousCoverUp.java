package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

@CardRegistration(set = "GRN", collectorNumber = "35")
@CardRegistration(set = "MID", collectorNumber = "48")
public class DeviousCoverUp extends Card {

    public DeviousCoverUp() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.EXILE));
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 4));
    }
}
