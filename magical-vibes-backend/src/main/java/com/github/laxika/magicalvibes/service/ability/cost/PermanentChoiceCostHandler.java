package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * Strategy interface for activated ability costs that require the player to choose one or more
 * permanents from their battlefield (e.g. "Sacrifice an artifact", "Tap an untapped blue creature").
 *
 * <p>Each implementation encapsulates the filtering, validation, and payment logic for a specific
 * cost type. {@link com.github.laxika.magicalvibes.service.ability.AbilityActivationService} uses
 * these handlers to unify the can-pay check, auto-selection (when choices are unambiguous), player
 * prompting, and completion flow for all permanent-choice costs.
 */
public interface PermanentChoiceCostHandler {

    /**
     * Returns the {@link CardEffect} record that this handler was created from.
     * Used to verify that the ability still contains the expected cost after async player input.
     */
    CardEffect costEffect();

    /**
     * Checks whether the player has enough valid permanents to pay this cost.
     *
     * @throws IllegalStateException if the cost cannot be paid
     */
    void validateCanPay(GameData gameData, UUID playerId);

    /**
     * Returns the IDs of all permanents on the player's battlefield that are valid choices
     * for this cost. The caller uses this list for auto-selection or to populate the choice prompt.
     */
    List<UUID> getValidChoiceIds(GameData gameData, UUID playerId);

    /**
     * Validates that the chosen permanent is a legal payment for this cost and executes the payment
     * (sacrifice, tap, etc.). Called once per chosen permanent.
     *
     * @throws IllegalStateException if the chosen permanent is not a valid payment
     */
    void validateAndPay(GameData gameData, Player player, Permanent chosen);

    /**
     * Returns the prompt message shown to the player when they must choose a permanent.
     *
     * @param remaining the number of permanents still to be chosen
     */
    String getPromptMessage(int remaining);

    /**
     * Returns the number of permanents the player must choose to fully pay this cost.
     * Typically 1 for single-sacrifice/tap costs, or N for {@link MultiplePermanentSacrificeCostHandler}.
     */
    int requiredCount();
}
