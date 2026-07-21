package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Exile target permanent, then [thenEffect]." The exile sibling of
 * {@link DestroyTargetPermanentThenEffect}: the targeted permanent is exiled and an existing
 * {@code thenEffect} is resolved afterwards, reusing that effect's own handler.
 *
 * <p>The then-effect is routed to the right player by {@link #recipient()}: {@code CONTROLLER}
 * resolves it against the spell/ability controller, while {@code TARGET_CONTROLLER} resolves it
 * against the controller of the exiled permanent — snapshotted before it leaves the battlefield, so
 * a follow-up like {@code SearchLibraryEffect} searches the exiled permanent's controller's library
 * without needing its own "target permanent's controller" variant. Exile always removes the
 * permanent (no regeneration / indestructible interaction), so the then-effect simply always
 * happens.
 *
 * <p>{@code thenCondition} makes the follow-up conditional on the exiled permanent: when non-null it
 * is evaluated against the target as last known on the battlefield (before exile), and the
 * then-effect runs only if it matches. Used by Gideon's Defeat: "Exile target white creature that's
 * attacking or blocking. If it was a Gideon planeswalker, you gain 5 life." — {@code GainLifeEffect}
 * gated on {@code PermanentHasSubtypePredicate(GIDEON)}. A {@code null} condition means the
 * then-effect always happens.
 *
 * <p>Used by Path to Exile: "Exile target creature. Its controller may search their library for a
 * basic land card, put that card onto the battlefield tapped, then shuffle." (A restricted search
 * can always fail to find, so "may search" needs no extra flag.)
 *
 * @param thenEffect    an existing effect resolved after exile (reused via its own handler)
 * @param recipient     whose controller slot the then-effect acts on
 * @param thenCondition optional predicate on the exiled permanent; {@code null} = always run
 */
public record ExileTargetPermanentThenEffect(
        CardEffect thenEffect,
        ThenEffectRecipient recipient,
        PermanentPredicate thenCondition
) implements CardEffect {

    /** Convenience: always run the then-effect (no condition on the exiled permanent). */
    public ExileTargetPermanentThenEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(thenEffect, recipient, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
