package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.List;
import java.util.UUID;

/**
 * What a library-to-battlefield effect does next with the permanents selected for it.
 *
 * <p>The follow-up is the card's own effect, written without the permanents: the choice handler records the permanents
 * put onto the battlefield as the picks of the entry the follow-up resolves in ({@code StackEntry.recordPicks}, and,
 * when there is one, as its chosen permanent), and the effect reads them from there.</p>
 */
public interface LibrarySelectionFollowUp {

    /** The follow-up's effect, which reads the selected permanents from the resolving entry's picks. */
    CardEffect effect();

    String prompt();

    /** Whether the follow-up is applicable to the selected permanents at this point in resolution. */
    default boolean shouldOffer(GameData gameData, List<UUID> selectedPermanentIds) {
        return true;
    }

    /** Whether the follow-up is offered as a separate reflexive may ability. */
    default boolean optional() {
        return true;
    }
}
