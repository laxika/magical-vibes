package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the source permanent protection from every color of the spell that caused its trigger: the spell's colors on
 * the stack, or its last-known colors once it has left (CR 608.2h), read from the stack entry's triggering spell.
 */
public record GrantProtectionFromTriggeringSpellColorsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
