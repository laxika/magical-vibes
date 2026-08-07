package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Target creature you don't control gets -2/-0 until end of turn and attacks this turn if able.
 * <p>
 * Overload {3}{U}{R} (CR 702.96a): paying the overload cost instead of {U}{R} changes "target" to
 * "each", so every creature its controller doesn't control gets -2/-0 and must attack this turn if
 * able and, per CR 702.96b, the spell chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "149")
public class ChemistersTrick extends Card {

    public ChemistersTrick() {
        var opponentCreatures = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());

        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new BoostTargetCreatureEffect(-2, 0),
                new BoostAllCreaturesEffect(-2, 0, opponentCreatures)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK),
                new MatchingCreaturesMustAttackThisTurnEffect(opponentCreatures)));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
