package com.github.laxika.magicalvibes.cards.d;

import java.util.List;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "123")
public class Devastation extends Card {

    public Devastation() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(
                List.of(new PermanentIsCreaturePredicate(), new PermanentIsLandPredicate()))));
    }
}
