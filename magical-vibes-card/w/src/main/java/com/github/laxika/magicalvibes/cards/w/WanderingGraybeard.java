package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "27")
public class WanderingGraybeard extends Card {

    public WanderingGraybeard() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, you gain 4 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new GainLifeEffect(4))));
    }
}
