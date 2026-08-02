package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "164")
public class BloodFrenzy extends Card {

    public BloodFrenzy() {
        // Cast this spell only before the combat damage step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_COMBAT_DAMAGE);

        // Target attacking or blocking creature gets +4/+0 until end of turn.
        // Destroy that creature at the beginning of the next end step.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be an attacking or blocking creature"
        ))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 0))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentAtEndStepEffect());
    }
}
