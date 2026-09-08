package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * "Target spell or permanent becomes {@link #color}." A one-shot effect that sets the target's
 * color indefinitely (CR 105.3 / CR 611.2b — no stated duration, so the continuous effect lasts as
 * long as the affected object exists), replacing all its previous colors (layer 5 setter). Used by the
 * "lace" instants (Purelace → white). A {@code null} color means the target becomes <em>colorless</em>
 * (the empty replacement color set of CR 105.3) — Ersatz Gnomes' "target spell becomes colorless".
 *
 * <p>Like {@link ChangeColorTextEffect} (Glamerdye) the default form targets a spell OR a permanent:
 * the permanent target is described by {@link #targetSpec()} ({@code PERMANENT}); the spell capability
 * is exposed through {@code EffectResolution.targetsSpellOnStack(effect)} and validated on the stack
 * path. With {@code spellOnly} the permanent half is dropped and the spec narrows to
 * {@code SPELL_ON_STACK}, so only a spell on the stack is a legal target. A color set on a permanent
 * spell carries onto the permanent it resolves into (CR 400.7a).
 */
public record SetTargetColorEffect(CardColor color, boolean spellOnly, boolean chooseColor)
        implements CardEffect, CombatOpponentReferencingEffect {

    /** "Target spell or permanent becomes [color]" — the lace instants. */
    public SetTargetColorEffect(CardColor color) {
        this(color, false, false);
    }

    public SetTargetColorEffect(CardColor color, boolean spellOnly) {
        this(color, spellOnly, false);
    }

    /** "Target spell becomes the color of your choice" — the resolution-time choice form. */
    public static SetTargetColorEffect chosenColorForSpell() {
        return new SetTargetColorEffect(null, true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(spellOnly ? TargetPredicates.spellOnStack() : TargetPredicates.permanent());
    }
}
