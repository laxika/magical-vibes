package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "62")
public class SlickImitator extends Card {

    public SlickImitator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new CopySpellEffect()),
                "Max speed — {1}, Sacrifice this creature: Copy target spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryControlledByPredicate(),
                        "Target must be a spell you control."
                )
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
