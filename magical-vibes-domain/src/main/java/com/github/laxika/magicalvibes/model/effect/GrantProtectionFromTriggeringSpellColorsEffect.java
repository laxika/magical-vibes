package com.github.laxika.magicalvibes.model.effect;

/** Grants the source permanent protection from every color of the spell that caused its trigger. */
public record GrantProtectionFromTriggeringSpellColorsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
