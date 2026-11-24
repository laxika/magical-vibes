package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this Equipment becomes attached to a creature, [effect]": a static ability of the Equipment, applied while it
 * becomes attached, without using the stack, like an "as [this permanent] enters" ability (CR 614.1c). Its effect is a
 * choice the Equipment remembers (a color, Sanctuary Blade; a creature card name and a creature type, Psychic Paper)
 * or steps resolved for the Equipment, the equipped creature as their triggering permanent, inside what is resolving
 * then: "choose an exiled creature card used to craft this Equipment" and the copy of it that a static "equipped
 * creature is a copy of the last chosen card" keeps while it stays attached (Dinosaur Headdress).
 *
 * @param effect what happens as it becomes attached
 */
public record AsThisBecomesAttachedEffect(CardEffect effect) implements ReplacementEffect {
}
