package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: ward abilities of creatures controlled by the controller's opponents do not
 * trigger while this effect is active. The trigger-collection service reads the marker; it does
 * not resolve on the stack.
 */
public record PreventOpponentCreatureWardTriggersEffect() implements CardEffect {
}
