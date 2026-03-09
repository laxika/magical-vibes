package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;

@CardRegistration(set = "NPH", collectorNumber = "127")
public class VorinclexVoiceOfHunger extends Card {

    public VorinclexVoiceOfHunger() {
        // Trample is auto-loaded from Scryfall
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddOneOfEachManaTypeProducedByLandEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new OpponentTappedLandDoesntUntapEffect());
    }
}
