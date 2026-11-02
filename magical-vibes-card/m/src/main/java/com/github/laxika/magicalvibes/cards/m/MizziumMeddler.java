package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "64")
public class MizziumMeddler extends Card {

    public MizziumMeddler() {
        // Flash — auto-loaded from Scryfall.
        //
        // When this creature enters, you may change a target of target spell or ability to this
        // creature. Same redirection as Spellskite; the target spell or ability is chosen as the
        // ETB ability goes on the stack via the ETB spell-target pipeline, and
        // StackEntryHasTargetPredicate signals that abilities on the stack are legal targets too.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryHasTargetPredicate(),
                "Target must be a spell or ability on the stack."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ChangeTargetOfTargetSpellToSourceEffect(),
                "Change a target of the spell or ability to this creature?"));
    }
}
