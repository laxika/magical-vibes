package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HOU", collectorNumber = "33")
public class CunningSurvivor extends Card {

    public CunningSurvivor() {
        // Whenever you cycle or discard a card, this creature gets +1/+0 until end of turn and can't be
        // blocked this turn. Cycling is a discard (CR 702.29e), so one controller-discard trigger covers
        // both wordings. The two self effects stay a single atomic triggered ability via SequenceEffect;
        // DiscardTriggerCollectorService enqueues it carrying the source permanent id, so both steps land
        // on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, SequenceEffect.of(
                new BoostSelfEffect(1, 0),
                new MakeCreatureUnblockableEffect(true)));
    }
}
