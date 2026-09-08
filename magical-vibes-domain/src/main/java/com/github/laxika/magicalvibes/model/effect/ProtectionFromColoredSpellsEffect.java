package com.github.laxika.magicalvibes.model.effect;

/** Grants protection from spells that have one or more colors. */
public record ProtectionFromColoredSpellsEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromColoredSpells() {
        return true;
    }
}
