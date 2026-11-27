package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AnyManaSpending;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * A resolved effect's permission to play (or cast) a card in exile (CR 611.2a): "you may play that card
 * this turn", "you may cast it for as long as it remains exiled". It is a floating effect on the exiled card
 * ({@code FloatingContinuousEffect.onCard}) whose duration is the one the card states; it ends as the card leaves
 * exile (CR 400.7). A card has at most one such permission; granting another replaces it.
 *
 * <p>What the permission adds to a play of that card is part of it: the cost it changes, the mana it lets be spent,
 * the life it lets be paid, the shared quota of a group of cards it was granted to.
 *
 * @param holderId                the player who may play the card
 * @param condition               a condition that must be met for the permission to apply, or {@code null}
 * @param spellFilter             restricts the characteristics of the spell cast with it, or {@code null}
 * @param anyManaType             mana may be spent to cast the card as {@link #manaSpending()} says
 * @param withoutPayingManaCost   the card may be played without paying its mana cost
 * @param costModifier            a cost change or replacement the play carries, or {@code null}
 * @param forLifeEqualToManaValue the spell may be cast by paying life equal to its mana value instead of mana
 * @param entersTapped            a land played with it enters tapped
 * @param groupId                 the shared quota group it counts against, or {@code null}
 *                                (see {@code GameData#registerExilePlayPermissionGroup})
 * @param staticSourcePermanentId the permanent whose static ability grants it, or {@code null}; the permission
 *                                applies only while that permanent keeps the ability (Uba Mask)
 * @param untilAnotherCard        it lasts until its source exiles another card with the same ability
 * @param asThoughAnyColor        with {@code anyManaType}, the "as though it were mana of any color" wording: a {C}
 *                                symbol is still paid only with colorless mana (CR 107.4c)
 */
public record ExilePlayPermissionEffect(UUID holderId, Condition condition, CardPredicate spellFilter,
                                        boolean anyManaType, boolean withoutPayingManaCost,
                                        ExilePlayCostModifier costModifier, boolean forLifeEqualToManaValue,
                                        boolean entersTapped, UUID groupId, UUID staticSourcePermanentId,
                                        boolean untilAnotherCard, boolean asThoughAnyColor) implements CardEffect {

    public ExilePlayPermissionEffect(UUID holderId, Condition condition, CardPredicate spellFilter,
                                     boolean anyManaType, boolean withoutPayingManaCost,
                                     ExilePlayCostModifier costModifier, boolean forLifeEqualToManaValue,
                                     boolean entersTapped, UUID groupId, UUID staticSourcePermanentId,
                                     boolean untilAnotherCard) {
        this(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost, costModifier,
                forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId, untilAnotherCard, false);
    }

    /** A plain permission for {@code holderId}. */
    public ExilePlayPermissionEffect(UUID holderId) {
        this(holderId, null, null, false, false, null, false, false, null, null, false);
    }

    public ExilePlayPermissionEffect withHolder(UUID holder) {
        return new ExilePlayPermissionEffect(holder, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId,
                untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withCondition(Condition value) {
        return new ExilePlayPermissionEffect(holderId, value, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId,
                untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withSpellFilter(CardPredicate value) {
        return new ExilePlayPermissionEffect(holderId, condition, value, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId,
                untilAnotherCard, asThoughAnyColor);
    }

    /** Mana of any type may be spent to cast the card ({@code value}), or no longer. */
    public ExilePlayPermissionEffect withAnyManaType(boolean value) {
        return withManaSpending(value ? AnyManaSpending.ANY_TYPE : AnyManaSpending.NONE);
    }

    /** Mana may be spent to cast the card as {@code spending} says. */
    public ExilePlayPermissionEffect withManaSpending(AnyManaSpending spending) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, spending.isAny(), withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId,
                untilAnotherCard, spending == AnyManaSpending.AS_THOUGH_ANY_COLOR);
    }

    /** How mana may be spent to cast the card. */
    public AnyManaSpending manaSpending() {
        return AnyManaSpending.of(anyManaType, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withWithoutPayingManaCost(boolean value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, value,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId,
                untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withCostModifier(ExilePlayCostModifier value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                value, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId, untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withForLifeEqualToManaValue(boolean value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, value, entersTapped, groupId, staticSourcePermanentId, untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withEntersTapped(boolean value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, value, groupId, staticSourcePermanentId, untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withGroupId(UUID value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, value, staticSourcePermanentId,
                untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withStaticSourcePermanentId(UUID value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, value, untilAnotherCard, asThoughAnyColor);
    }

    public ExilePlayPermissionEffect withUntilAnotherCard(boolean value) {
        return new ExilePlayPermissionEffect(holderId, condition, spellFilter, anyManaType, withoutPayingManaCost,
                costModifier, forLifeEqualToManaValue, entersTapped, groupId, staticSourcePermanentId, value, asThoughAnyColor);
    }
}
