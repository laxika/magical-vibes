package com.github.laxika.magicalvibes.model.effect;

/**
 * Fatal Lore. An opponent chooses one — either the controller draws three cards, or the controller
 * destroys up to two creatures that opponent controls (they can't be regenerated) and that opponent
 * draws up to three cards. The choosing opponent is prompted through the may-ability
 * (accept/decline) system: accept is the draw-three mode, decline is the destroy mode.
 */
public record FatalLoreEffect() implements CardEffect {
}
