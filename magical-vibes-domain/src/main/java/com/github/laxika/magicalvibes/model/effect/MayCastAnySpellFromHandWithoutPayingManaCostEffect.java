package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 * (Maelstrom Archangel — any spell; Wildfire Eternal — instant or sorcery)
 *
 * <p>On resolution the controller is offered each matching nonland hand card as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast. A {@code null}
 * {@code spellFilter} matches every nonland hand card.
 *
 * @param spellFilter which hand cards are eligible ({@code null} = any nonland)
 * @param maxManaValue maximum eligible mana value ({@code null} = no maximum)
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                                  DynamicAmount maxManaValue)
        implements CardEffect, CombatDamageAmountAwareEffect {

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                               DynamicAmount maxManaValue) {
        this.spellFilter = spellFilter;
        this.maxManaValue = maxManaValue;
    }

    /** Any nonland spell (Maelstrom Archangel). */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect() {
        this(null, null);
    }

    /** A matching nonland spell, capped by a dynamic mana value. */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(DynamicAmount maxManaValue) {
        this(null, maxManaValue);
    }

    /** A matching nonland spell with no mana-value cap. */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter) {
        this(spellFilter, null);
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return maxManaValue;
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        if (maxManaValue == null) {
            return this;
        }
        CardPredicate manaValueFilter = new CardMaxManaValuePredicate(damageDealt);
        CardPredicate combinedFilter = spellFilter == null
                ? manaValueFilter
                : new CardAllOfPredicate(List.of(spellFilter, manaValueFilter));
        return new MayCastAnySpellFromHandWithoutPayingManaCostEffect(combinedFilter, null);
    }
}
