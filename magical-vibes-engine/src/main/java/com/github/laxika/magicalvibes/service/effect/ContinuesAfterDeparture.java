package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * An effect handler whose instruction removes a permanent and then goes on ("sacrifice it, then …", "destroy it,
 * then …"). It departs through {@code StepDepartureSupport.departThenContinue}, and everything after the departure
 * lives in {@link #continueAfterDeparture}, which reads what happened from the entry's resolution scratchpad
 * ({@link StackEntry#getResolutionResults()}). It runs right away when the departure is done, or as the next
 * resolution step when the departure ended the step on a CR 616.1 question.
 */
public interface ContinuesAfterDeparture {

    /** Does what {@code effect} does after its departure; the departure's results are already recorded. */
    void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect);
}
