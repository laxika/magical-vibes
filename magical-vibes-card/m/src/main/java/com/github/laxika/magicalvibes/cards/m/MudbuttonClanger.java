package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "95")
public class MudbuttonClanger extends Card {

    public MudbuttonClanger() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, this creature
        // gets +1/+1 until end of turn.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new BoostSelfEffect(1, 1))));
    }
}
