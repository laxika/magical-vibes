package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RagingRiverEffect;

@CardRegistration(set = "2ED", collectorNumber = "169")
public class RagingRiver extends Card {

    public RagingRiver() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new RagingRiverEffect());
    }
}
