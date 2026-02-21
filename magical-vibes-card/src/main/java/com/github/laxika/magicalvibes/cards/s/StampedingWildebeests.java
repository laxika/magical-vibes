package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "300")
public class StampedingWildebeests extends Card {

    public StampedingWildebeests() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new BounceCreatureOnUpkeepEffect(
                BounceCreatureOnUpkeepEffect.Scope.SOURCE_CONTROLLER,
                Set.of(new ControlledPermanentPredicateTargetFilter(
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                        "Target must be a green creature you control"
                )),
                "Choose a green creature you control to return to its owner's hand."
        ));
    }
}
