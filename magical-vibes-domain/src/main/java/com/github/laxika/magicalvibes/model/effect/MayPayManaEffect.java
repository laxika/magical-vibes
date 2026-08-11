package com.github.laxika.magicalvibes.model.effect;

/**
 * Like {@link MayEffect}, but the player must pay a mana cost to get the effect.
 * Used for "you may pay {X}. If you do, [effect]" patterns (e.g. Spellbomb cycle).
 *
 * <p>{@code payer} redirects the "may pay" prompt away from the ability's controller:
 * {@link MayPayPayer#ENCHANTED_CONTROLLER} prompts the enchanted permanent's controller — the
 * player carried on the stack entry's {@code targetId} by an
 * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} trigger (Paralyze) — and
 * {@link MayPayPayer#DEFENDING_PLAYER} prompts the player being attacked, read from an
 * {@code ON_ATTACK} trigger's {@code attackedTargetId} (Mtenda Lion).
 *
 * <p>{@code elseEffect} models the "If you don't, [effect]" half of a punisher-style choice — it
 * resolves instead of {@code wrapped} when the payment is declined or cannot be made (Preferred
 * Selection). Leave it {@code null} for the plain "if you do" shape, where declining does nothing.
 *
 * <p>{@code lifeCost} is life paid <em>in addition to</em> the mana ("you may pay {4} and 2 life" —
 * Purgatory). Both halves must be payable or the whole payment fails; leave it {@code 0} for the
 * mana-only shape. This is not the "pay {M} or N life" choice — that is
 * {@link TargetPlayerMayPayManaOrLifeEffect}.
 *
 * <p>{@code sourceIsTriggeringPermanent} marks an enter-the-battlefield trigger whose wrapped
 * effect uses the entering permanent as its source rather than the permanent with this ability.
 */
public record MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt,
                               MayPayPayer payer, CardEffect elseEffect, int lifeCost,
                               boolean sourceIsTriggeringPermanent)
        implements CombatDamageTriggerContextEffect {

    /**
     * Delegates to the wrapped effect, like {@link MayEffect}: the target of "you may pay {X}. If
     * you do, [targeted effect]" is chosen when the ability goes on the stack (CR 603.3d), while
     * the payment choice happens at resolution (CR 603.5). When {@code wrapped} is null (pay-to-
     * avoid / punisher shape) or itself non-targeting, falls back to {@code elseEffect}'s spec —
     * "you may pay {X}. If you don't, destroy target …" (Knight of the Mists).
     */
    @Override
    public TargetSpec targetSpec() {
        if (wrapped != null) {
            TargetSpec wrappedSpec = wrapped.targetSpec();
            if (wrappedSpec != TargetSpec.NONE) {
                return wrappedSpec;
            }
        }
        if (elseEffect != null) {
            return elseEffect.targetSpec();
        }
        return TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return wrapped instanceof CombatDamageTriggerContextEffect contextEffect
                ? contextEffect.combatDamageTriggerContext()
                : null;
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt) {
        this(manaCost, wrapped, prompt, MayPayPayer.CONTROLLER, null, 0, false);
    }

    public MayPayManaEffect(String manaCost, int lifeCost, CardEffect wrapped, String prompt) {
        this(manaCost, wrapped, prompt, MayPayPayer.CONTROLLER, null, lifeCost, false);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, MayPayPayer payer) {
        this(manaCost, wrapped, prompt, payer, null, 0, false);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, CardEffect elseEffect) {
        this(manaCost, wrapped, prompt, MayPayPayer.CONTROLLER, elseEffect, 0, false);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt, MayPayPayer payer,
                            CardEffect elseEffect, int lifeCost) {
        this(manaCost, wrapped, prompt, payer, elseEffect, lifeCost, false);
    }

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt,
                            boolean sourceIsTriggeringPermanent) {
        this(manaCost, wrapped, prompt, MayPayPayer.CONTROLLER, null, 0,
                sourceIsTriggeringPermanent);
    }
}
