package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Marker payload for an emblem that reads "At the beginning of your upkeep, …" (Chandra, Roaring
 * Flame's ultimate emblem). It is never resolved directly: it lives inside a
 * {@link CreateEmblemEffect}'s static-effect list, and {@code StepTriggerService} scans the emblems
 * controlled by the active player at the beginning of their upkeep and puts {@link #effects} onto the
 * stack as a triggered ability of the emblem.
 *
 * @param effects       what the emblem does when it triggers, resolved as a non-targeting triggered
 *                      ability controlled by the emblem's controller
 * @param reminderText  the emblem's own text, used in the game log
 */
public record EmblemUpkeepTriggerEffect(List<CardEffect> effects, String reminderText) implements CardEffect {

    public EmblemUpkeepTriggerEffect {
        effects = List.copyOf(effects);
    }
}
