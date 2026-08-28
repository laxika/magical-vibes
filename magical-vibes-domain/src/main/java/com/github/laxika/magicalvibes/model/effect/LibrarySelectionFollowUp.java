package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Creates a reflexive ability from permanents selected for a library-to-battlefield effect. */
public interface LibrarySelectionFollowUp {

    CardEffect createEffect(List<UUID> selectedPermanentIds);

    String prompt();
}
