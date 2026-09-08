package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: each spell the controller casts that matches {@code filter} has
 * {@code grantedAbility}, so its cost may be paid even though the card lacks the printed keyword.
 * Consulted alongside the innate keyword by the cost gates in the spell-casting flow.
 * <p>
 * Used by Wort, the Raidmother (conspire, on red or green instant and sorcery spells), Chief
 * Engineer (convoke, on artifact spells), and Silverquill, the Disputant (casualty 1, on instants
 * and sorceries).
 * <p>
 * Only the abilities those gates actually query are accepted: a grant nothing consults would be
 * silently inert, so widening this set means wiring a new gate at the same time.
 */
public record GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, int abilityValue,
                                                      CardPredicate filter)
        implements SpellCastingAbilityGrantingEffect {

    public GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter) {
        this(grantedAbility, 0, filter);
    }

    public GrantSpellCastingAbilityToSpellsEffect {
        if (grantedAbility != Keyword.CONSPIRE && grantedAbility != Keyword.CONVOKE
                && grantedAbility != Keyword.CASUALTY) {
            throw new IllegalArgumentException(
                    "No cast-cost gate consults a granted " + grantedAbility
                            + "; only CONSPIRE, CONVOKE, and CASUALTY do");
        }
        if (grantedAbility == Keyword.CASUALTY && abilityValue < 1) {
            throw new IllegalArgumentException("Casualty value must be positive");
        }
        if (grantedAbility != Keyword.CASUALTY && abilityValue != 0) {
            throw new IllegalArgumentException("Only casualty grants have a value");
        }
    }
}
