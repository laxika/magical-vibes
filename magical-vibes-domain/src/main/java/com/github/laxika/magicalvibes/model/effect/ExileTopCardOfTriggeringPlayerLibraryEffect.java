package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the player identified by the resolving trigger entry. As an
 * {@code Triggers.dealsCombatDamageToPlayer()} ability the entry binds the damaged player (ingest).
 *
 * @param triggeringCardOwner whether the player is instead the owner of the card a "whenever you play a card" event was
 *                            about ("that card's owner … exiles the top card of their library", The Matrix of Time),
 *                            read from the entry as the ability resolves ({@link #ofTriggeringCardOwner()})
 */
public record ExileTopCardOfTriggeringPlayerLibraryEffect(boolean triggeringCardOwner)
        implements CardEffect {

    public ExileTopCardOfTriggeringPlayerLibraryEffect() {
        this(false);
    }

    /** "That card's owner exiles the top card of their library", of the card the trigger event was about. */
    public static ExileTopCardOfTriggeringPlayerLibraryEffect ofTriggeringCardOwner() {
        return new ExileTopCardOfTriggeringPlayerLibraryEffect(true);
    }
}
