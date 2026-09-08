package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * "Choose a creature type. All creatures of that type get +P/+T until end of turn," optionally
 * with keyword grants in the same effect.
 *
 * <p>The creature type is chosen during resolution and stored temporarily on
 * {@code GameData.chosenSpellSubtype}; the effect then applies a one-shot modifier to every
 * matching creature on the battlefield. The amounts are evaluated once after the creature type
 * is chosen, and any keywords are granted to that same snapshot of creatures.</p>
 */
public record BoostAllCreaturesOfChosenSubtypeEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        Set<Keyword> keywords
) implements CardEffect, KeywordGrantingEffect {

    public BoostAllCreaturesOfChosenSubtypeEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }

    public BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), Set.of());
    }

    public BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost,
                                                  Set<Keyword> keywords) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), keywords);
    }

    public BoostAllCreaturesOfChosenSubtypeEffect {
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.ALL_CREATURES;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
