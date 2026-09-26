package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerToughnessTotalAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerToughnessTotalAtMostPredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "12")
public class ScaledDestruction extends Card {

    public ScaledDestruction() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all small creatures",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerToughnessTotalAtMostPredicate(4))))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all medium creatures",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerToughnessTotalAtLeastPredicate(5),
                                new PermanentPowerToughnessTotalAtMostPredicate(8))))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all large creatures",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerToughnessTotalAtLeastPredicate(9)))))
        )));
    }
}
