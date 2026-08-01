package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Street Spasm deals X damage to target creature without flying you don't control.
 * <p>
 * Overload {X}{X}{R}{R} (CR 702.96a): paying the overload cost instead of {X}{R} changes "target"
 * to "each", so the spell deals X damage to each creature without flying its controller doesn't
 * control and, per CR 702.96b, chooses no targets at all. X is chosen once and paid twice
 * (CR 107.3b), so the damage is X, not 2X.
 */
@CardRegistration(set = "RTR", collectorNumber = "106")
public class StreetSpasm extends Card {

    public StreetSpasm() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{X}{X}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DealDamageToTargetCreatureEffect(new XValue()),
                new MassDamageEffect(new XValue(), false, false, notYoursAndWithoutFlying())));
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), notYoursAndWithoutFlying())),
                "Target must be a creature without flying you don't control"));
    }

    private static PermanentAllOfPredicate notYoursAndWithoutFlying() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
