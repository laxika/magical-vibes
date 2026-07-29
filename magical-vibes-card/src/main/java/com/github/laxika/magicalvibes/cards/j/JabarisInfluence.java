package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "24")
public class JabarisInfluence extends Card {

    public JabarisInfluence() {
        // Cast this spell only after combat.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.AFTER_COMBAT);
        // Gain control of target nonartifact, nonblack creature that attacked you this turn
        // and put a -1/-0 counter on it.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                        new PermanentAttackedSourceControllerThisTurnPredicate()
                )),
                "Target must be a nonartifact, nonblack creature that attacked you this turn"
        ))
                .addEffect(EffectSlot.SPELL, new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ZERO, 1));
    }
}
