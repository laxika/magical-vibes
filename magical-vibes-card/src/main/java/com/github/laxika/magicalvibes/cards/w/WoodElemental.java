package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "215")
public class WoodElemental extends Card {

    public WoodElemental() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                new PermanentNotPredicate(new PermanentIsTappedPredicate())))));
    }
}
