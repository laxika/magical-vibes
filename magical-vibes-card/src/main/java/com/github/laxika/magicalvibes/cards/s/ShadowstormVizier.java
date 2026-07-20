package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "AKH", collectorNumber = "206")
public class ShadowstormVizier extends Card {

    public ShadowstormVizier() {
        // Whenever you cycle or discard a card, this creature gets +1/+1 until end of turn. Cycling is a
        // discard (CR 702.29e), so a single "controller discards" trigger covers both wordings; the
        // BoostSelfEffect is enqueued as a triggered ability by DiscardTriggerCollectorService.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new BoostSelfEffect(1, 1));
    }
}
