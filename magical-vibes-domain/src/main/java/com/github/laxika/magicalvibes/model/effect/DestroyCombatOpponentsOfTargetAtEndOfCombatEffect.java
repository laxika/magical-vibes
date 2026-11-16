package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot end-of-combat destruction keyed to a chosen creature. The default mode destroys all
 * creatures that blocked or were blocked by it this turn (Venomous Breath); the directional mode
 * only destroys creatures blocked by it (Glyph of Doom).
 * <p>
 * At resolution a delayed ability that lasts this turn ("at this turn's next end of combat") is registered about the
 * target and the creatures that blocked or were blocked by it so far. As the next end of combat step begins it goes
 * on the stack carrying this effect with the creature as its non-targeting object; that
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
                                                                boolean onlyWhileInCombat,
                                                                boolean ofSource)
        implements CardEffect, RemovalEffect {

    public DestroyCombatOpponentsOfTargetAtEndOfCombatEffect() {
        this(false, false, false);
    }

    public DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(boolean onlyCreaturesBlockedByTarget) {
        this(onlyCreaturesBlockedByTarget, false, false);
    }

    /**
     * "At end of combat, destroy all creatures blocking or blocked by this creature" (Kjeldoran Frostbeast): the
     * source's own end-of-combat ability, which targets nothing, destroying only while the source is still in combat.
     */
    public static DestroyCombatOpponentsOfTargetAtEndOfCombatEffect ofSourceWhileInCombat() {
        return new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(false, true, true);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        return ofSource ? TargetSpec.NONE : TargetSpec.harmful(TargetPredicates.creature());
    }
}
