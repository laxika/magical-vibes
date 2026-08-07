package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: each spell the controller casts that matches {@code filter} has
 * {@code grantedAbility}, so its cost may be paid even though the card lacks the printed keyword.
 * Consulted alongside the innate keyword by the cost gates in the spell-casting flow.
 * <p>
 * Used by Wort, the Raidmother (conspire, CR 702.78, on red or green instant and sorcery spells)
 * and Chief Engineer (convoke, CR 702.51, on artifact spells).
 * <p>
 * Only the abilities those gates actually query are accepted: a grant nothing consults would be
 * silently inert, so widening this set means wiring a new gate at the same time.
 */
public record GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter)
        implements SpellCastingAbilityGrantingEffect {

    public GrantSpellCastingAbilityToSpellsEffect {
        if (grantedAbility != Keyword.CONSPIRE && grantedAbility != Keyword.CONVOKE) {
            throw new IllegalArgumentException(
                    "No cast-cost gate consults a granted " + grantedAbility + "; only CONSPIRE and CONVOKE do");
        }
    }
}
