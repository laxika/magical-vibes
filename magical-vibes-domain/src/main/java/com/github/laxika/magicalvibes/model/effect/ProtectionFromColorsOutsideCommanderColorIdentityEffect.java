package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants an equipped creature protection from every color outside its controller's commander
 * color identity. The protected colors are derived dynamically from the controller's command zone
 * and commander permanents on the battlefield.
 */
public record ProtectionFromColorsOutsideCommanderColorIdentityEffect() implements CardEffect {
}
