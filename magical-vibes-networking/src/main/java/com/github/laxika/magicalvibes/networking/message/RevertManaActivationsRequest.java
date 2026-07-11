package com.github.laxika.magicalvibes.networking.message;

/**
 * MTGO-style "cancel casting": undo the player's still-revertable mana-ability
 * activations (tapped sources untap, the mana they produced leaves the pool).
 */
public record RevertManaActivationsRequest() {
}
