package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Delayed trigger that, at the beginning of the controller's next main phase (precombat or
 * postcombat), adds {@code amount} mana of {@code color}.
 *
 * <p>When {@code optional} is true (Scattering Stroke's clash-win reward), the delayed ability is
 * wrapped in a {@code MayEffect} so the player may decline. When false (Conduit of Storms /
 * Conduit of Emrakul), mana is added automatically.
 *
 * <p>When {@code thisTurnOnly} is true, the delayed action is cleared at cleanup if it never fired
 * ("at the beginning of your next main phase this turn").
 *
 * <p>When {@code anyColorCombination} is true the mana is "in any combination of colors" — the
 * controller picks a color per mana and {@code color} is ignored (Plasm Capture). When
 * {@code firstMainOnly} is true the trigger fires only on a precombat main phase ("your next
 * <em>first</em> main phase"), not on a postcombat main.
 */
public record AddManaAtNextMainPhase(
        UUID controllerId,
        ManaColor color,
        int amount,
        Card sourceCard,
        boolean optional,
        boolean thisTurnOnly,
        boolean anyColorCombination,
        boolean firstMainOnly) implements DelayedAction {

    /** Scattering Stroke-style: optional, persists until the controller's next main phase. */
    public AddManaAtNextMainPhase(UUID controllerId, ManaColor color, int amount, Card sourceCard) {
        this(controllerId, color, amount, sourceCard, true, false, false, false);
    }

    public AddManaAtNextMainPhase(UUID controllerId, ManaColor color, int amount, Card sourceCard,
                                  boolean optional, boolean thisTurnOnly) {
        this(controllerId, color, amount, sourceCard, optional, thisTurnOnly, false, false);
    }
}
