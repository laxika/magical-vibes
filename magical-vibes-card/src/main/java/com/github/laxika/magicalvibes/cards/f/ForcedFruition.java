package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

@CardRegistration(set = "LRW", collectorNumber = "66")
public class ForcedFruition extends Card {

    public ForcedFruition() {
        // Whenever an opponent casts a spell, that player draws seven cards.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new DrawCardForTargetPlayerEffect(7));
    }
}
