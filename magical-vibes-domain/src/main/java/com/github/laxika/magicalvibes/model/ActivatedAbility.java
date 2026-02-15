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

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, String description) {
        this(requiresTap, manaCost, effects, needsTarget, false, description, null);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, String description, TargetFilter targetFilter) {
        this(requiresTap, manaCost, effects, needsTarget, false, description, targetFilter);
    }

    public ActivatedAbility(boolean requiresTap, String manaCost, List<CardEffect> effects, boolean needsTarget, boolean needsSpellTarget, String description, TargetFilter targetFilter) {
        this.requiresTap = requiresTap;
        this.manaCost = manaCost;
        this.effects = effects;
        this.needsTarget = needsTarget;
        this.needsSpellTarget = needsSpellTarget;
        this.description = description;
        this.targetFilter = targetFilter;
    }
}
