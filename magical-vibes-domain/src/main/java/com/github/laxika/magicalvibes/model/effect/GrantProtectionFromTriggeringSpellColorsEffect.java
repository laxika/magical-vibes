package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import java.util.Set;

/** Grants the source permanent protection from every color of the spell that caused its trigger. */
public record GrantProtectionFromTriggeringSpellColorsEffect(Set<CardColor> triggeringColors) implements CardEffect {

    public GrantProtectionFromTriggeringSpellColorsEffect() {
        this(null);
    }

    public GrantProtectionFromTriggeringSpellColorsEffect {
        triggeringColors = triggeringColors == null ? null : Set.copyOf(triggeringColors);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
