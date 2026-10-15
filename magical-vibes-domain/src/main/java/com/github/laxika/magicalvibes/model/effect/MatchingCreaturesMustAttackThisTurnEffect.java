package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot, non-targeted: creatures matching {@code matcher} (any controller) attack this turn if
 * able. The predicate is evaluated with the effect's source controller as {@code sourceControllerId},
 * so "creatures your opponents control attack this turn if able" is this effect with
 * {@code PermanentNotPredicate(PermanentControlledBySourceControllerPredicate())}.
 *
 * <p>By default the requirement doesn't change characteristics or control, so it modifies the game
 * rules (CR 611.2c): it is registered as a turn-long {@link MatchingCreaturesMustAttackEffect} and also
 * binds creatures that enter or start matching later this turn (Grisly Anglerfish, Goblin Diplomats).
 *
 * @param lockedAtResolution {@code true} when the text names a fixed set of objects ("those
 *                           creatures", "this creature", overloaded "each creature ... gets -2/-0
 *                           and attacks"): only creatures matching when the effect resolves get the
 *                           transient {@code mustAttackThisTurn} flag (Suicidal Charge, Chemister's
 *                           Trick, Kookus)
 */
public record MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher, boolean lockedAtResolution)
        implements CardEffect {

    public MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher) {
        this(matcher, false);
    }
}
