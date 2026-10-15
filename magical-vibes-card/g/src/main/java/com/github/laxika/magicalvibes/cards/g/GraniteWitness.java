package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "206")
public class GraniteWitness extends Card {

    public GraniteWitness() {
        addMorph("{W/U}{W/U}");
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "Tap or untap target creature?"));
    }
}
