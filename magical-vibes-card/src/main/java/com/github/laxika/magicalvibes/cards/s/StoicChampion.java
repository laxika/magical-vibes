package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "LGN", collectorNumber = "21")
@CardRegistration(set = "VMA", collectorNumber = "50")
public class StoicChampion extends Card {

    public StoicChampion() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CYCLES, new BoostSelfEffect(2, 2));
    }
}
