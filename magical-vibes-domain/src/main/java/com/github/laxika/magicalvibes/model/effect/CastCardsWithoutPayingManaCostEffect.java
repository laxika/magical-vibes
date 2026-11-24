package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * "You may cast any number of spells from among [those cards] without paying their mana costs", "you may cast up to
 * three spells from among …", "… with total mana value X or less …": the cast-now step. As the effect resolves, the
 * controller picks at once which of the offered spells to cast and in which order (CR 608.2g), each is cast during the
 * resolution with no player getting priority after it, and each cast begins only if it may
 * ({@code CastingPermissionService.canCastDuringResolution}, CR 601.3). Casting a spell without paying its mana cost is
 * its alternative cost (CR 118.9), so no other alternative cost applies (CR 118.9a); its additional costs are still
 * paid (CR 118.8). A card not cast stays where it is.
 *
 * <p>The offered cards are those {@link #cards} names that are in exile, face up and spell cards (a land card can't be
 * cast; a face-down card has no characteristics, CR 406.3a). Narrow them with the subject's own filter
 * ({@code Subjects.exiledCards(eachOpponent(), filter)}, {@code cardsThisWay(EXILE, filter)}).
 *
 * @param cards          the cards the spells are cast from among
 * @param maxCount       how many spells may be cast at most ({@link Integer#MAX_VALUE} for any number)
 * @param totalManaValue the most their total mana value may be, evaluated once as the effect resolves, each split
 *                       card counting only the half cast (CR 709.3a); {@code null} for no limit
 */
public record CastCardsWithoutPayingManaCostEffect(CardSubject cards, int maxCount, DynamicAmount totalManaValue)
        implements CardEffect {

    public CastCardsWithoutPayingManaCostEffect {
        Objects.requireNonNull(cards, "cards");
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }

    /** "You may cast any number of spells from among {@code cards} without paying their mana costs." */
    public static CastCardsWithoutPayingManaCostEffect anyNumber(CardSubject cards) {
        return new CastCardsWithoutPayingManaCostEffect(cards, Integer.MAX_VALUE, null);
    }

    /** "You may cast up to {@code count} spells from among {@code cards} without paying their mana costs." */
    public static CastCardsWithoutPayingManaCostEffect upTo(int count, CardSubject cards) {
        return new CastCardsWithoutPayingManaCostEffect(cards, count, null);
    }

    /** "… with total mana value {@code total} or less …". */
    public CastCardsWithoutPayingManaCostEffect withTotalManaValueAtMost(DynamicAmount total) {
        return new CastCardsWithoutPayingManaCostEffect(cards, maxCount, Objects.requireNonNull(total, "total"));
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }
}
