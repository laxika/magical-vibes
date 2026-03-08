package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "159")
public class Spellskite extends Card {

    public Spellskite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U/P}",
                List.of(new ChangeTargetOfTargetSpellToSourceEffect()),
                "{U/P}: Change a target of target spell or ability to Spellskite.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryHasTargetPredicate(),
                        "Target must be a spell or ability on the stack."
                )
        ));
    }
}
