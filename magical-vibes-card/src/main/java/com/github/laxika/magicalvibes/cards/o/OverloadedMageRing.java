package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

public class OverloadedMageRing extends Card {

    public OverloadedMageRing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), CopySpellEffect.permanentSpellBecomesToken()),
                "{1}, {T}, Sacrifice Overloaded Mage-Ring: Copy target spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryControlledByPredicate(),
                        "Target must be a spell you control.")));
    }
}
