package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "54")
public class CoralTrickster extends Card {

    public CoralTrickster() {
        addMorph("{U}");
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new TapOrUntapTargetPermanentEffect());
    }
}
