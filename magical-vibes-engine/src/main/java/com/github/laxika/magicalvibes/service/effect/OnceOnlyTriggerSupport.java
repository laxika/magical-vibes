package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Tracks the attack abilities that "trigger only once" for a permanent object (the unkeyed mark the attack collector
 * judges); every other "triggers only once" is keyed by its ability ({@code TriggeredAbilityStackService}).
 */
public final class OnceOnlyTriggerSupport {

    private OnceOnlyTriggerSupport() {
    }

    public static boolean isFired(GameData gameData, Permanent source) {
        return gameData.onceOnlyTriggersFired.contains(source.getId());
    }

    public static void mark(GameData gameData, Permanent source) {
        gameData.onceOnlyTriggersFired.add(source.getId());
    }
}
