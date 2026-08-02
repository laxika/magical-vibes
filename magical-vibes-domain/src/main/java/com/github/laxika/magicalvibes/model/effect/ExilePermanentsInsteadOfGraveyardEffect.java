package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that exiles permanents instead of letting them enter a graveyard.
 * The replacement only applies to permanents leaving the battlefield, not to permanent cards in
 * other zones.
 */
public record ExilePermanentsInsteadOfGraveyardEffect() implements CardEffect {
}
