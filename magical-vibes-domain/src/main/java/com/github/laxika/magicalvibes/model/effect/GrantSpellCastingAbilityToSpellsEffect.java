package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: each spell the controller casts that matches {@code filter} has
 * {@code grantedAbility} even though the card lacks the printed ability.
 * Consulted alongside the innate ability by the spell-casting flow.
 * <p>
 * Used by Wort, the Raidmother (conspire, on red or green instant and sorcery spells), Chief Engineer
 * (convoke, on artifact spells), and Inspiring Statuary (improvise, on nonartifact spells).
 * <p>
 * Only abilities with engine support are accepted: a grant nothing consults would be silently
 * inert, so widening this set means wiring a new gate at the same time.
 */
public record GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter,
                                                     Zone sourceZone)
        implements SpellCastingAbilityGrantingEffect {

    public GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter) {
        this(grantedAbility, filter, null);
    }

    public static GrantSpellCastingAbilityToSpellsEffect fromZone(Keyword grantedAbility,
                                                                   CardPredicate filter,
                                                                   Zone sourceZone) {
        return new GrantSpellCastingAbilityToSpellsEffect(grantedAbility, filter, sourceZone);
    }

    public GrantSpellCastingAbilityToSpellsEffect {
        if (grantedAbility != Keyword.CONSPIRE
                && grantedAbility != Keyword.CONVOKE
                && grantedAbility != Keyword.IMPROVISE
                && grantedAbility != Keyword.REBOUND
                && grantedAbility != Keyword.DELVE) {
            throw new IllegalArgumentException(
                    "No cast flow consults a granted " + grantedAbility
                            + "; only CONSPIRE, CONVOKE, IMPROVISE, REBOUND, and DELVE do");
        }
    }
}
