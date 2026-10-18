package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller privately looks at the top card of their own library. The card stays on top and
 * is not revealed to the other players. Pair with a conditional effect reading the top card for an
 * "if it's a land card, you may reveal it" rider (Plane-Merge Elf).
 */
public record LookAtTopCardOfControllersLibraryEffect() implements CardEffect {
}
