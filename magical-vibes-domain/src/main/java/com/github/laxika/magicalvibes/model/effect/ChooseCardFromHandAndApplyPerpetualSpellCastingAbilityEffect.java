package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Chooses a matching card in hand and perpetually grants it a valued spell-casting ability. */
public record ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffect(
        Keyword grantedAbility, int abilityValue, CardPredicate cardFilter) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffect {
        Objects.requireNonNull(grantedAbility, "grantedAbility");
        Objects.requireNonNull(cardFilter, "cardFilter");
        new GrantSpellCastingAbilityToSpellsEffect(grantedAbility, abilityValue, cardFilter);
    }
}
