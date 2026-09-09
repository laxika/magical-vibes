package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.List;
import java.util.UUID;

/** Creates a reflexive ability from permanents selected for a library-to-battlefield effect. */
public interface LibrarySelectionFollowUp {

    CardEffect createEffect(List<UUID> selectedPermanentIds);

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
