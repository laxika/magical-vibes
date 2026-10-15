package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanSourceControllerHandSizePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourceControllerHandSizePredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "253")
@CardRegistration(set = "C19", collectorNumber = "20")
@CardRegistration(set = "WOC", collectorNumber = "114")
public class NightmareUnmaking extends Card {

    public NightmareUnmaking() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile each creature with power greater than the number of cards in your hand",
                        new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerGreaterThanSourceControllerHandSizePredicate())))),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile each creature with power less than the number of cards in your hand",
                        new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerLessThanSourceControllerHandSizePredicate()))))
        )));
    }
}
