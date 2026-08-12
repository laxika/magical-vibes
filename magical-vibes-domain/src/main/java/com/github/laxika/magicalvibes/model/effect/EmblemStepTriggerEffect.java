package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Marker payload for an emblem that reads "At the beginning of your {step}, …" or "At the
 * beginning of each opponent's upkeep, …" (Chandra, Roaring Flame's upkeep emblem, Sarkhan, the
 * Dragonspeaker's draw-step emblem, Ajani, Adversary of Tyrants' end-step emblem). It is never resolved
 * directly: it lives inside a {@link CreateEmblemEffect}'s static-effect list, and
 * {@code StepTriggerService} scans the emblems and applies the step's controller/opponent relation
 * at the beginning of that step, then puts {@link #effects} onto the stack as a triggered ability
 * of the emblem.
 *
 * @param step          the step the emblem triggers in, on its controller's turn
 * @param effects       what the emblem does when it triggers, resolved as a non-targeting triggered
 *                      ability controlled by the emblem's controller
 * @param reminderText  the emblem's own text, used in the game log
 */
public record EmblemStepTriggerEffect(EmblemTriggerStep step, List<CardEffect> effects,
                                      String reminderText) implements CardEffect {

    public EmblemStepTriggerEffect {
        effects = List.copyOf(effects);
    }
}
