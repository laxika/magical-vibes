package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "192")
@CardRegistration(set = "GPT", collectorNumber = "122")
@CardRegistration(set = "DDK", collectorNumber = "25")
@CardRegistration(set = "E02", collectorNumber = "40")
@CardRegistration(set = "SLD", collectorNumber = "1801")
@CardRegistration(set = "TSR", collectorNumber = "381")
@CardRegistration(set = "ACR", collectorNumber = "96")
@CardRegistration(set = "CMD", collectorNumber = "211")
@CardRegistration(set = "LTC", collectorNumber = "269")
public class Mortify extends Card {

    public Mortify() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be a creature or enchantment"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
