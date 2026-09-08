package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "10")
public class Depopulate extends Card {

    public Depopulate() {
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(new Min(
                new Fixed(1),
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsMulticoloredPredicate())),
                        CountScope.CONTROLLER))));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
