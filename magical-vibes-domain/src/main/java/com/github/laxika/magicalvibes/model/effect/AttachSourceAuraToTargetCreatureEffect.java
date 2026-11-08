package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the source Aura onto a target creature (CR 303.4 attach). The Aura is expected to already be
 * on the battlefield; resolving reattaches it to {@code targetId} and gives it a new timestamp.
 * Reads {@code targetId} as the creature to attach to and {@code sourcePermanentId} as the Aura.
 * With {@code triggeringPermanent} the creature is the stack entry's triggering permanent, read as the
 * ability resolves and not targeted (Prison Term's "you may attach this Aura to that creature").
 *
 * @param triggeringPermanent whether the creature is the stack entry's triggering permanent rather than its
 *                            {@code targetId}
 */
public record AttachSourceAuraToTargetCreatureEffect(boolean triggeringPermanent) implements CardEffect {

    public AttachSourceAuraToTargetCreatureEffect() {
        this(false);
    }

    /** "Attach this Aura to that creature", that creature the stack entry's triggering permanent. */
    public static AttachSourceAuraToTargetCreatureEffect ofTriggeringPermanent() {
        return new AttachSourceAuraToTargetCreatureEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return triggeringPermanent ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent());
    }
}
