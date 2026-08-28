package com.github.laxika.magicalvibes.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Progress state for Forgotten Lore-style effects' "target opponent chooses a card in your
 * graveyard; you may pay to repeat, excluding cards already chosen" loop.
 */
public class ForgottenLoreState {

    public boolean active;
    /** Cards the opponent has already chosen this resolution; they can't be chosen again. */
    public final Set<UUID> chosenCardIds = new LinkedHashSet<>();
    /** The most recently chosen card — the one put into the controller's hand when the loop ends. */
    public UUID lastChosenCardId;
    /** Card just picked by the opponent, consumed on the next re-entry. */
    public UUID pendingChosenCardId;
    /** Chosen "pay {G}" / "don't pay" option, consumed on the next re-entry. */
    public String chosenMode;

    public void reset() {
        active = false;
        chosenCardIds.clear();
        lastChosenCardId = null;
        pendingChosenCardId = null;
        chosenMode = null;
    }
}
