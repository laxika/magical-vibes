package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "AKH", collectorNumber = "103")
public class PitilessVizier extends Card {

    public PitilessVizier() {
        // Whenever you cycle or discard a card, this creature gains indestructible until end of turn.
        // Cycling is a discard (CR 702.29e), so a single "controller discards" trigger covers both
        // wordings; the GrantScope.SELF grant is enqueued as a triggered ability by
        // DiscardTriggerCollectorService and resolves against the source permanent.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF));
    }
}
