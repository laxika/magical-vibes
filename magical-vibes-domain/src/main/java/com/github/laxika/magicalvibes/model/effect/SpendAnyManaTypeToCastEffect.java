package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;
import java.util.Set;

/**
 * Static: "you can spend mana of any type to cast creature spells" (Vizier of the Menagerie), "you may spend mana as
 * though it were mana of any color to cast planeswalker spells / Case spells / spells you don't own" (Oath of Nissa,
 * Case File Auditor, Nathan Drake). While a permanent with it is on the battlefield, its controller may pay the colored
 * mana requirements of a spell {@link #spellFilter()} matches with mana of any type (CR 609.4b: only how the cost is
 * paid changes). Read by {@code CastingPermissionService.canSpendAnyManaTypeFromBattlefield}, the filter judged with
 * the casting player as its perspective ({@code CardControllerDoesNotOwnPredicate} is "a spell you don't own").
 *
 * @param spellFilter which spells it lets be paid that way
 */
public record SpendAnyManaTypeToCastEffect(CardPredicate spellFilter) implements CardEffect {

    public SpendAnyManaTypeToCastEffect {
        Objects.requireNonNull(spellFilter, "spellFilter");
    }

    /** "To cast [type] spells". */
    public SpendAnyManaTypeToCastEffect(Set<CardType> spellTypes) {
        this(new CardAnyOfPredicate(spellTypes.stream()
                .sorted()
                .<CardPredicate>map(CardTypePredicate::new)
                .toList()));
    }

    /** "To cast [filter] spells". */
    public static SpendAnyManaTypeToCastEffect spells(CardPredicate spellFilter) {
        return new SpendAnyManaTypeToCastEffect(spellFilter);
    }
}
