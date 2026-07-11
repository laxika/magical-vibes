package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "PTK", collectorNumber = "2")
public class EightfoldMaze extends Card {

    public EightfoldMaze() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // Destroy target attacking creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
