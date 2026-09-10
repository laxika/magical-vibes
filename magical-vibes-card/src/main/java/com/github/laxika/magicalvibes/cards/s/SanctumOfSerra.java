package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "OHOP", collectorNumber = "32")
public class SanctumOfSerra extends Card {

    public SanctumOfSerra() {
        addEffect(EffectSlot.PLANESWALK_FROM_TRIGGERED,
                new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new MayEffect(new SetLifeTotalEffect(20), "Have your life total become 20?"));
    }
}
