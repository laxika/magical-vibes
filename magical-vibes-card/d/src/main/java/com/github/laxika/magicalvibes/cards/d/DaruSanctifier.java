package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "9")
public class DaruSanctifier extends Card {

    public DaruSanctifier() {
        addMorph("{1}{W}");
        target(TargetFilters.enchantment()).addEffect(
                EffectSlot.ON_TURNED_FACE_UP,
                new DestroyTargetPermanentEffect());
    }
}
