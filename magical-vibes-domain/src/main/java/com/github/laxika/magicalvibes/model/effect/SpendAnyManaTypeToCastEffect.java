package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;
import java.util.Set;

/**
 * Static: "you can spend mana of any type to cast creature spells" (Vizier of the Menagerie) or, with
 * {@link #asThoughAnyColor()}, "you may spend mana as though it were mana of any color to cast planeswalker spells / Case
 * spells / spells you don't own" (Oath of Nissa, Case File Auditor, Nathan Drake). While a permanent with it is on the
 * battlefield, its controller may pay the colored mana requirements of a spell {@link #spellFilter()} matches with mana of
 * any type (CR 609.4b: only how the cost is paid changes). Any type also pays a {C} symbol; any color doesn't, as {C} is
 * paid only with colorless mana (CR 106.1b, 107.4c). Read by {@code CastingPermissionService.canSpendAnyManaTypeFromBattlefield}
 * and {@code canSpendManaAsAnyColorToCastFromBattlefield}, the filter judged with the casting player as its perspective
 * ({@code CardControllerDoesNotOwnPredicate} is "a spell you don't own").
 *
 * @param spellFilter       which spells it lets be paid that way
 * @param asThoughAnyColor  mana is spent as though it were mana of any color, not of any type
 */
public record SpendAnyManaTypeToCastEffect(CardPredicate spellFilter, boolean asThoughAnyColor) implements CardEffect {

    public SpendAnyManaTypeToCastEffect {
        Objects.requireNonNull(spellFilter, "spellFilter");
    }

    /** "You can spend mana of any type to cast [type] spells". */
    public SpendAnyManaTypeToCastEffect(Set<CardType> spellTypes) {
        this(new CardAnyOfPredicate(spellTypes.stream()
                .sorted()
                .<CardPredicate>map(CardTypePredicate::new)
                .toList()), false);
    }

    /** "You may spend mana as though it were mana of any color to cast [filter] spells". */
    public static SpendAnyManaTypeToCastEffect asThoughAnyColor(CardPredicate spellFilter) {
        return new SpendAnyManaTypeToCastEffect(spellFilter, true);
    }
}
