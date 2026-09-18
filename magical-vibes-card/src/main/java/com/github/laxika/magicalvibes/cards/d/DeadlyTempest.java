package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C15", collectorNumber = "19")
public class DeadlyTempest extends Card {

    public DeadlyTempest() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                new EachDestroyedPermanentControllerLosesLifeEffect()));
    }
}
