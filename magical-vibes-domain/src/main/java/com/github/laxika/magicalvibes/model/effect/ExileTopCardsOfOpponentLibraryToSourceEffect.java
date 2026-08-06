package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: an opponent exiles the top N cards of their library, tracked as "exiled with"
 * the source permanent (Grimoire Thief, Nightveil Specter).
 *
 * <p>The opponent-library counterpart of {@link ExileTopCardsToSourceEffect} (controller's own
 * library) and {@link EachPlayerExilesTopCardsToSourceEffect} (every player). In a two-player game
 * the single opponent is the only legal target, so no separate target choice is required.
 *
 * @param count    how many cards to exile
 * @param faceDown whether the cards are exiled face down (Grimoire Thief) or face up
 *                 (Nightveil Specter)
 */
public record ExileTopCardsOfOpponentLibraryToSourceEffect(int count, boolean faceDown)
        implements CombatDamageTriggerContextEffect {

    /**
     * As a combat-damage trigger the exiling player is the damaged player, and the exiled cards
     * have to be tracked with the damage-dealing permanent, so both must be bound on the entry.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
