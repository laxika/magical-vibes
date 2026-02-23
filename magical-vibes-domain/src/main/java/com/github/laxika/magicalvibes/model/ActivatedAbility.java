package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

import java.util.List;

@Getter
public class ActivatedAbility {

    private final boolean requiresTap;
    private final String manaCost;
    private final List<CardEffect> effects;
    private final String description;
    private final TargetFilter targetFilter;
    private final Integer loyaltyCost;
    private final Integer maxActivationsPerTurn;
    private final ActivationTimingRestriction timingRestriction;

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description) {
        this(requiresTap, manaCost, effects, description, null, null, null, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, description, targetFilter, null, null, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, Integer maxActivationsPerTurn) {
        this(requiresTap, manaCost, effects, description, null, null, maxActivationsPerTurn, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, description, null, null, null, timingRestriction);
    }

    // Loyalty ability constructor
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description) {
        this(false, null, effects, description, null, loyaltyCost, null, null);
    }

    // Loyalty ability constructor with target filter
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, String description, TargetFilter targetFilter) {
        this(false, null, effects, description, targetFilter, loyaltyCost, null, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, String description, TargetFilter targetFilter, Integer loyaltyCost, Integer maxActivationsPerTurn, ActivationTimingRestriction timingRestriction) {
        this.requiresTap = requiresTap;
        this.manaCost = manaCost;
        this.effects = effects;
        this.description = description;
        this.targetFilter = targetFilter;
        this.loyaltyCost = loyaltyCost;
        this.maxActivationsPerTurn = maxActivationsPerTurn;
        this.timingRestriction = timingRestriction;
    }

    public boolean isNeedsTarget() {
        return effects.stream().anyMatch(e -> e.canTargetPlayer() || e.canTargetPermanent() || e.canTargetGraveyard());
    }

    public boolean isNeedsSpellTarget() {
        return effects.stream().anyMatch(CardEffect::canTargetSpell);
    }
}
