package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "93")
public class ClatteringSkeletons extends Card {

    public ClatteringSkeletons() {
        addEffect(EffectSlot.ON_DEATH, new VentureIntoDungeonEffect());
    }
}
