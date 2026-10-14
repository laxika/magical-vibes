package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the permanent that fired this triggered ability ("sacrifice that permanent"), read
 * from {@code StackEntry.triggeringPermanentId}. Used by Equipment with sacrifice-on-unattach
 * (Grafted Wargear, Stitcher's Graft). A player can sacrifice only permanents they control, so
 * nothing happens when the triggering permanent is controlled by another player; if it has already
 * left the battlefield, nothing happens either.
 */
public record SacrificeTriggeringPermanentEffect() implements CardEffect {
}
