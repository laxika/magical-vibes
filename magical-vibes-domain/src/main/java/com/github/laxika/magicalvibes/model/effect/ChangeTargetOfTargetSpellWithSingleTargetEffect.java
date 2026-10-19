package com.github.laxika.magicalvibes.model.effect;

/**
 * Redirects a spell that has exactly one target to a new legal target chosen by this effect's controller.
 *
 * @param newTargetKind controls whether the new target may be any legal target, only a creature,
 *                      or only a player
 * @param requiresSourceTarget whether the spell's original target must be the source permanent
 * @param allowsRepeatedSourceTarget when {@code requiresSourceTarget} is set, whether the spell may have
 *                                   several targets as long as every one of them is the source permanent;
 *                                   when false the spell must have exactly one target
 * @param requiresTargetIsController whether the spell's single target must be the effect's controller
 *                                   ("if that target is you"), checked on resolution
 */
public record ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind newTargetKind,
                                                               boolean requiresSourceTarget,
                                                               boolean allowsRepeatedSourceTarget,
                                                               boolean requiresTargetIsController) implements CardEffect {

    public ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind newTargetKind,
                                                           boolean requiresSourceTarget,
                                                           boolean allowsRepeatedSourceTarget) {
        this(newTargetKind, requiresSourceTarget, allowsRepeatedSourceTarget, false);
    }

    public enum NewTargetKind {
        ANY,
        CREATURE,
        PLAYER
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect() {
        this(NewTargetKind.ANY, false, false);
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind newTargetKind) {
        this(newTargetKind, false, false);
    }

    public ChangeTargetOfTargetSpellWithSingleTargetEffect(boolean creatureTargetsOnly) {
        this(creatureTargetsOnly ? NewTargetKind.CREATURE : NewTargetKind.ANY, false, false);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect playersOnly() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.PLAYER, false, false);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect playersOnlyIfTargetIsController() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.PLAYER, false, false, true);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect sourceCreatureTargetsOnly() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.CREATURE, true, true);
    }

    public static ChangeTargetOfTargetSpellWithSingleTargetEffect sourceCreatureSingleTarget() {
        return new ChangeTargetOfTargetSpellWithSingleTargetEffect(NewTargetKind.CREATURE, true, false);
    }

    public boolean creatureTargetsOnly() {
        return newTargetKind == NewTargetKind.CREATURE;
    }

    public boolean playerTargetsOnly() {
        return newTargetKind == NewTargetKind.PLAYER;
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
