package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a regeneration shield (CR 701.15).
 * <p>
 * {@code opponentMayDrawOnRegenerate} is Soldevi Sentry's rider: "When it regenerates this way, that
 * player may draw a card." The draw is owed only when the shield is actually spent, so it rides on
 * the shield ({@code Permanent.opponentDrawRegenerationShield}) rather than resolving with the
 * ability — a sibling {@code TargetOpponentMayDrawCardEffect} would hand over the card even if the
 * creature never regenerated.
 * <p>
 * {@code putMinusOneCounterOnRegenerate} is Matopi Golem's rider: "When it regenerates this way,
 * put a -1/-1 counter on it." Same shield-tagging pattern
 * ({@code Permanent.minusOneCounterRegenerationShield}).
 * <p>
 * {@code regeneratesEnchantedCreature} is for an ability that regenerates the creature the source is
 * attached to even though the source is not necessarily an Aura right now — Nurturing Licid keeps
 * "{G}: Regenerate enchanted creature" while it is still a creature, where the ability simply does
 * nothing. Plain Auras (Regeneration, Carapace) do not need the flag: the execution service already
 * redirects a self-targeting regenerate on an Aura or Equipment to the attached creature.
 */
public record RegenerateEffect(
        boolean targetsPermanent,
        boolean opponentMayDrawOnRegenerate,
        boolean putMinusOneCounterOnRegenerate,
        boolean regeneratesEnchantedCreature
) implements RegenerationEffect {

    public RegenerateEffect() {
        this(false, false, false, false);
    }

    public RegenerateEffect(boolean targetsPermanent) {
        this(targetsPermanent, false, false, false);
    }

    public RegenerateEffect(boolean targetsPermanent, boolean opponentMayDrawOnRegenerate) {
        this(targetsPermanent, opponentMayDrawOnRegenerate, false, false);
    }

    public RegenerateEffect(boolean targetsPermanent,
                            boolean opponentMayDrawOnRegenerate,
                            boolean putMinusOneCounterOnRegenerate) {
        this(targetsPermanent, opponentMayDrawOnRegenerate, putMinusOneCounterOnRegenerate, false);
    }

    /** Matopi Golem: regenerate self; put a -1/-1 counter only when the shield is spent. */
    public static RegenerateEffect withMinusOneCounterOnRegenerate() {
        return new RegenerateEffect(false, false, true, false);
    }

    /** Nurturing Licid: regenerate the attached creature, or nothing while unattached. */
    public static RegenerateEffect enchantedCreature() {
        return new RegenerateEffect(false, false, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (targetsPermanent) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        return regeneratesEnchantedCreature
                ? TargetSpec.NONE
                : new TargetSpec(null, false, null, true, 1);
    }
}
