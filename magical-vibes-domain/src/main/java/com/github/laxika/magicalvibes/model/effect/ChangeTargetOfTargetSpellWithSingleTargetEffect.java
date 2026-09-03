package com.github.laxika.magicalvibes.model.effect;

/**
 * Redirects a spell that has exactly one target to a new legal target chosen by this effect's controller.
 *
 * @param newTargetKind controls whether the new target may be any legal target, only a creature,
 *                      or only a player
 * @param requiresSourceTarget whether the spell's original target must be the source permanent
 */
public record ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind newTargetKind,
                                                               boolean requiresSourceTarget) implements CardEffect {

    public enum NewTargetKind {
        ANY,
        CREATURE,
        PLAYER
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect() {
        this(NewTargetKind.ANY, false);
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind newTargetKind) {
        this(newTargetKind, false);
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect(boolean creatureTargetsOnly) {
        this(creatureTargetsOnly ? NewTargetKind.CREATURE : NewTargetKind.ANY, false);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect playersOnly() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.PLAYER, false);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect sourceCreatureTargetsOnly() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.CREATURE, true);
    }

    public boolean creatureTargetsOnly() {
        return newTargetKind == NewTargetKind.CREATURE;
    }

    public boolean playerTargetsOnly() {
        return newTargetKind == NewTargetKind.PLAYER;
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
