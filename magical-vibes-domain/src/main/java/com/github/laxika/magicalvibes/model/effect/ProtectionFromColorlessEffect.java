package com.github.laxika.magicalvibes.model.effect;

/** Grants protection from colorless: from every source that has no color (CR 105.2c). */
public record ProtectionFromColorlessEffect() implements ProtectionGrantingEffect {

    @Override
    public boolean protectionFromColorless() {
        return true;
    }
}
