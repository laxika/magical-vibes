package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect: return the card of the permanent that was put into a graveyard from the
 * battlefield back to a player's hand (fizzles if the card left that graveyard, e.g. it was a
 * token). Used by Enduring Renewal — "Whenever a creature is put into your graveyard from the
 * battlefield, return it to your hand." — and by Yomiji, Who Bars the Way — "Whenever a legendary
 * permanent other than Yomiji is put into a graveyard from the battlefield, return that card to
 * its owner's hand."
 *
 * <p>The card to return is the stack entry's triggering card. Card definitions use the no-arg ctor; the
 * trigger collectors bake {@code handOwnerId}.
 *
 * @param handOwnerId the player whose graveyard is searched and whose hand receives the card;
 *                    {@code null} means the ability controller (Enduring Renewal's "your hand")
 */
public record ReturnTriggeringCardToOwnerHandEffect(UUID handOwnerId) implements CardEffect {

    public ReturnTriggeringCardToOwnerHandEffect() {
        this(null);
    }
}
