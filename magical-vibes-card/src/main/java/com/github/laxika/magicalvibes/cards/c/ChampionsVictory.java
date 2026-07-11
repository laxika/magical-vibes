package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "PTK", collectorNumber = "39")
public class ChampionsVictory extends Card {

    public ChampionsVictory() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // Return target attacking creature to its owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
