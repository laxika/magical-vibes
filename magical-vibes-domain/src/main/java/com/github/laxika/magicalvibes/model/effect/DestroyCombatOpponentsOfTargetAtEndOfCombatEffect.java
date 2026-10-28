package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot end-of-combat destruction keyed to a chosen creature. The default mode destroys all
 * creatures that blocked or were blocked by it this turn (Venomous Breath); the directional mode
 * only destroys creatures blocked by it (Glyph of Doom).
 * <p>
 * At resolution a {@link com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat}
 * delayed action is queued for the target. As the next end of combat step begins,
 * {@code CombatService.processEndOfCombatCombatOpponentDestructions()} puts a delayed trigger
 * carrying this effect on the stack with the creature as its source permanent and no target; that
 * trigger computes the creatures to destroy as it resolves, so blocks declared after this spell
 * resolved count too; regeneration and indestructible apply.
 * <p>
 * Unlike {@link DestroyCombatOpponentAtEndOfCombatEffect}, which is a Basilisk-style combat trigger
 * on the source permanent, this is a one-shot spell effect keyed to a chosen target.
 * <p>
 * With {@code onlyWhileInCombat}, the end-of-combat trigger destroys nothing when its source is no longer
 * attacking or blocking, as for Kjeldoran Frostbeast's "destroy all creatures blocking or blocked by this
 * creature". A regenerated creature is removed from combat (CR 506.4). Without it, the turn's combat history
 * is used regardless of the source's current combat status, as for Gaze of the Gorgon's "blocked or were
 * blocked by it this turn".
 */
public record DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(boolean onlyCreaturesBlockedByTarget,
                                                                boolean onlyWhileInCombat)
        implements CardEffect, RemovalEffect {

    public DestroyCombatOpponentsOfTargetAtEndOfCombatEffect() {
        this(false, false);
    }

    public DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(boolean onlyCreaturesBlockedByTarget) {
        this(onlyCreaturesBlockedByTarget, false);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
