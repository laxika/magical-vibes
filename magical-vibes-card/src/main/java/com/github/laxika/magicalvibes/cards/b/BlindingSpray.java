package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KTK", collectorNumber = "32")
public class BlindingSpray extends Card {

    public BlindingSpray() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-4, 0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
