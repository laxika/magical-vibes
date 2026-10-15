package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;

@CardRegistration(set = "DIS", collectorNumber = "16")
public class ProperBurial extends Card {

    public ProperBurial() {
        // Whenever a creature you control dies, you gain life equal to that creature's toughness.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new GainLifeEqualToDyingCreatureToughnessEffect());
    }
}
