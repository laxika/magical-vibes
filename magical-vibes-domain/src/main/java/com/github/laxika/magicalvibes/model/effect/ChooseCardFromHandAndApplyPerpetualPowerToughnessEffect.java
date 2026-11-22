package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/**
 * "Choose a [matching] card in your hand. It perpetually [gets +N/+N][, gains a keyword][, incorporates a mana cost]
 * [and gains 'when you cast this spell, ...']": the controller alone sees and answers the hand choice, whose answer
 * applies the changes the effect has.
 *
 * @param cardFilter      which hand cards may be chosen
 * @param power           the perpetual power change, or 0
 * @param toughness       the perpetual toughness change, or 0
 * @param keywords        the keywords perpetually gained, or none
 * @param manaCost        the mana cost perpetually incorporated, or {@code null}
 * @param selfCastAbility what the card's perpetually gained "when you cast this spell" ability does, or {@code null}
 */
public record ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(
        CardPredicate cardFilter, int power, int toughness, Set<Keyword> keywords, String manaCost,
        CardEffect selfCastAbility) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        keywords = Set.copyOf(keywords);
        if (manaCost != null && manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }

    public ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(CardPredicate cardFilter, int power, int toughness) {
        this(cardFilter, power, toughness, Set.of(), null, null);
    }

    /** "It perpetually gains [keywords]." */
    public static ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect gaining(CardPredicate cardFilter,
                                                                                  Set<Keyword> keywords) {
        return new ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(cardFilter, 0, 0, keywords, null, null);
    }

    /** "It perpetually incorporates [manaCost] and gains 'When you cast this spell, [selfCastAbility]'." */
    public static ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect incorporating(CardPredicate cardFilter,
                                                                                        String manaCost,
                                                                                        CardEffect selfCastAbility) {
        return new ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(cardFilter, 0, 0, Set.of(),
                Objects.requireNonNull(manaCost, "manaCost"),
                Objects.requireNonNull(selfCastAbility, "selfCastAbility"));
    }

    /** "It perpetually incorporates [manaCost] and gets +N/+N." */
    public static ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect incorporatingAndGetting(
            CardPredicate cardFilter, String manaCost, int power, int toughness) {
        return new ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(cardFilter, power, toughness, Set.of(),
                Objects.requireNonNull(manaCost, "manaCost"), null);
    }
}
