package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "48")
public class RiptideChimera extends Card {

    public RiptideChimera() {
        // At the beginning of your upkeep, return an enchantment you control to its owner's hand.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment you control"
                )),
                "Choose an enchantment you control to return to its owner's hand."
        ));
    }
}
