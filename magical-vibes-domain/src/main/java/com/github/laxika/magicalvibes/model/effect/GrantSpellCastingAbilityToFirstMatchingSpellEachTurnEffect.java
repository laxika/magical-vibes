package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect: the first matching spell cast by the source controller each turn has an extra casting ability. */
public record GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
        Keyword grantedAbility, CardPredicate filter, Zone sourceZone, int abilityValue)
        implements SpellCastingAbilityGrantingEffect {

    public GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
            Keyword grantedAbility, CardPredicate filter) {
        this(grantedAbility, filter, null, 0);
    }

    public GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
            Keyword grantedAbility, int abilityValue, CardPredicate filter) {
        this(grantedAbility, filter, null, abilityValue);
    }

    public GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
            Keyword grantedAbility, CardPredicate filter, Zone sourceZone) {
        this(grantedAbility, filter, sourceZone, 0);
    }

    public GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect {
        if (grantedAbility != Keyword.CONSPIRE
                && grantedAbility != Keyword.CONVOKE
                && grantedAbility != Keyword.IMPROVISE
                && grantedAbility != Keyword.REBOUND
                && grantedAbility != Keyword.DELVE
                && grantedAbility != Keyword.JUMP_START
                && grantedAbility != Keyword.CASUALTY
                && grantedAbility != Keyword.REPLICATE) {
            throw new IllegalArgumentException(
                    "No cast flow consults a granted " + grantedAbility
                            + "; only CONSPIRE, CONVOKE, IMPROVISE, REBOUND, DELVE, JUMP_START, and REPLICATE do");
        }
    }

    @Override
    public boolean appliesOnlyToFirstMatchingSpellEachTurn() {
        return true;
    }
}
