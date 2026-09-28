package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "WHO", collectorNumber = "137")
public class JennyGeneratedAnomaly extends Card {

    public JennyGeneratedAnomaly() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExploreEffect());
    }
}
