package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "8")
public class DaruMender extends Card {

    public DaruMender() {
        addMorph("{W}");
        target(TargetFilters.creature()).addEffect(
                EffectSlot.ON_TURNED_FACE_UP,
                new RegenerateEffect(true));
    }
}
