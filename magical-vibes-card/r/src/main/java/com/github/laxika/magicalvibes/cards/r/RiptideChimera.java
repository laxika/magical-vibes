package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "48")
public class RiptideChimera extends Card {

    public RiptideChimera() {
        // At the beginning of your upkeep, return an enchantment you control to its owner's hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment you control"
                )),
                "Choose an enchantment you control to return to its owner's hand."
        ));
    }
}
