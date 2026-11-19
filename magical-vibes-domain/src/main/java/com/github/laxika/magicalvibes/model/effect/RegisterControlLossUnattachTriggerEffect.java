package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates the delayed triggered ability "when you lose control of that Equipment, unattach it" about the Equipment the
 * resolving ability targets (CR 603.7a, 603.7c).
 *
 * @param thisTurn whether it is "when you lose control of that Equipment this turn, if it's attached to a creature you
 *                 control, unattach it" (Stolen Uniform): it ends with the turn (CR 603.7b) and its "if" is judged as it
 *                 triggers and resolves (CR 603.4); otherwise it has no stated duration (Ogre Geargrabber)
 */
public record RegisterControlLossUnattachTriggerEffect(boolean thisTurn)
        implements CardEffect {

    /** "When you lose control of that Equipment, unattach it." */
    public static RegisterControlLossUnattachTriggerEffect whenControlLost() {
        return new RegisterControlLossUnattachTriggerEffect(false);
    }

    /** "When you lose control of that Equipment this turn, if it's attached to a creature you control, unattach it." */
    public static RegisterControlLossUnattachTriggerEffect thisTurnIfAttachedToCreatureYouControl() {
        return new RegisterControlLossUnattachTriggerEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
