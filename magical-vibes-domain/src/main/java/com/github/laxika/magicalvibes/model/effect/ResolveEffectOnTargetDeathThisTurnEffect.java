package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Creates a delayed triggered ability (CR 603.7a): when the targeted permanent dies this turn, the wrapped effect
 * resolves under the registering player's control. Its source is the source of the spell or ability that created it
 * (CR 603.7e), so self-referential effects (e.g. {@link TransformToBackFaceEffect}, Initiate of Blood) still find it,
 * and the dying permanent is its triggering permanent as it last existed (CR 603.10a), with its last controller as
 * "that creature's controller". Used by Skeletonize and Initiate of Blood.
 *
 * @param effect the effect to resolve when the targeted permanent dies this turn
 * @param onlyIfDealtDamageBySource when true the delayed trigger is only registered if the source of this
 *                                  spell or ability actually dealt damage to the target ("a creature dealt
 *                                  damage this way dies this turn", Skeletonize), so prevented damage
 *                                  registers nothing
 * @param interveningIf the "if" after the trigger condition (Fight for the Throne), or {@code null}
 */
public record ResolveEffectOnTargetDeathThisTurnEffect(CardEffect effect, boolean onlyIfDealtDamageBySource,
                                                       Condition interveningIf)
        implements CardEffect {

    public ResolveEffectOnTargetDeathThisTurnEffect(CardEffect effect) {
        this(effect, false, null);
    }

    /** "When a creature dealt damage this way dies this turn, ..." */
    public static ResolveEffectOnTargetDeathThisTurnEffect ifDealtDamageThisWay(CardEffect effect) {
        return new ResolveEffectOnTargetDeathThisTurnEffect(effect, true, null);
    }

    /**
     * "When that creature dies this turn, if [condition], [effect]": the "if" is checked as the ability triggers and
     * again as it resolves (CR 603.4).
     */
    public static ResolveEffectOnTargetDeathThisTurnEffect ifCondition(Condition interveningIf, CardEffect effect) {
        return new ResolveEffectOnTargetDeathThisTurnEffect(effect, false, interveningIf);
    }
}
