package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

import java.util.List;

@Getter
public class ActivatedAbility {

    private final boolean requiresTap;
    private final String manaCost;
    private final List<CardEffect> effects;
    private final boolean needsTarget;
    private final boolean needsSpellTarget;
    private final String description;
    private final TargetFilter targetFilter;
    private final Integer loyaltyCost;
    private final ActivationTimingRestriction timingRestriction;

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, String description) {
        this(requiresTap, manaCost, effects, needsTarget, false, description, null, null, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, needsTarget, false, description, targetFilter, null, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, boolean needsSpellTarget, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, needsTarget, needsSpellTarget, description, targetFilter, null, null);
    }

    // Loyalty ability constructor
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, boolean needsTarget, String description) {
        this(false, null, effects, needsTarget, false, description, null, loyaltyCost, null);
    }

    // Loyalty ability constructor with target filter
    public ActivatedAbility(int loyaltyCost, List<CardEffect> effects, boolean needsTarget, String description, TargetFilter targetFilter) {
        this(false, null, effects, needsTarget, false, description, targetFilter, loyaltyCost, null);
    }

    // Constructor with timing restriction
    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, String description, ActivationTimingRestriction timingRestriction) {
        this(requiresTap, manaCost, effects, needsTarget, false, description, null, null, timingRestriction);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, boolean needsSpellTarget, String description, TargetFilter targetFilter, Integer loyaltyCost) {
        this(requiresTap, manaCost, effects, needsTarget, needsSpellTarget, description, targetFilter, loyaltyCost, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, boolean needsSpellTarget, String description, TargetFilter targetFilter, Integer loyaltyCost, ActivationTimingRestriction timingRestriction) {
        this.requiresTap = requiresTap;
        this.manaCost = manaCost;
        this.effects = effects;
        this.needsTarget = needsTarget;
        this.needsSpellTarget = needsSpellTarget;
        this.description = description;
        this.targetFilter = targetFilter;
        this.loyaltyCost = loyaltyCost;
        this.timingRestriction = timingRestriction;
    }
}
