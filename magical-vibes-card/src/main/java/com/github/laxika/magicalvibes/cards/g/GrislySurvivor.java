package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "HOU", collectorNumber = "64")
public class GrislySurvivor extends Card {

    public GrislySurvivor() {
        // Whenever you cycle or discard a card, this creature gets +2/+0 until end of turn. Cycling is a
        // discard (CR 702.29e), so a single "controller discards" trigger covers both wordings; the
        // BoostSelfEffect is enqueued as a triggered ability by DiscardTriggerCollectorService.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new BoostSelfEffect(2, 0));
    }
}
