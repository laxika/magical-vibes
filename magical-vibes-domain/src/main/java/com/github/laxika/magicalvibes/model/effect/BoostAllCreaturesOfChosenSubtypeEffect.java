package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * "Choose a creature type. All creatures of that type get +P/+T until end of turn," optionally
 * with keyword grants in the same effect. The scope can be narrowed to creatures controlled by
 * the spell's controller.
 *
 * <p>The creature type is chosen during resolution and stored temporarily on
 * {@code GameData.chosenSpellSubtype}; the effect then applies a one-shot modifier to every
 * matching creature on the battlefield. The amounts are evaluated once after the creature type
 * is chosen, and any keywords are granted to that same snapshot of creatures.</p>
 */
public record BoostAllCreaturesOfChosenSubtypeEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        Set<Keyword> keywords,
        GrantScope scope
) implements CardEffect, KeywordGrantingEffect {

    public BoostAllCreaturesOfChosenSubtypeEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of(), GrantScope.ALL_CREATURES);
    }

    public BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), Set.of(), GrantScope.ALL_CREATURES);
    }

    public BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost,
                                                  Set<Keyword> keywords) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), keywords, GrantScope.ALL_CREATURES);
    }

    public static BoostAllCreaturesOfChosenSubtypeEffect ownCreatures(
            int powerBoost, int toughnessBoost, Set<Keyword> keywords) {
        return new BoostAllCreaturesOfChosenSubtypeEffect(
                new Fixed(powerBoost), new Fixed(toughnessBoost), keywords, GrantScope.OWN_CREATURES);
    }

    public BoostAllCreaturesOfChosenSubtypeEffect {
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
        scope = scope == null ? GrantScope.ALL_CREATURES : scope;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
