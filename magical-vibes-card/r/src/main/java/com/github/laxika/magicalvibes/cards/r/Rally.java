package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "ICE", collectorNumber = "48")
public class Rally extends Card {

    public Rally() {
        // Blocking creatures get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(1, 1, new PermanentIsBlockingPredicate()));
    }
}
