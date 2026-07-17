package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;

import java.util.UUID;

/**
 * Handles one {@link PendingInteraction} kind end to end: prompting the deciding player
 * (both when the interaction begins and for prompt replay on reconnect), and validating and
 * applying the player's answer. Implementations are Spring beans auto-registered into the
 * {@link InteractionHandlerRegistry} by {@code GameEngineConfig} (same discovery pattern as
 * the effect handler registries).
 *
 * <p>The deciding player and the legal answer space live on the interaction record itself
 * ({@link PendingInteraction#decidingPlayerId()} / {@link PendingInteraction#legalOptions()}),
 * not on the handler.
 */
public interface InteractionHandler<T extends PendingInteraction> {

    /** The interaction record class this handler owns. */
    Class<T> handledType();

    /** The wire-payload shape this handler accepts; other shapes fall through to legacy dispatch. */
    Class<? extends InteractionAnswer> answerType();

    /**
     * Sends the prompt message for this interaction to {@code recipientId}. Called with the
     * mind-control-resolved recipient when the interaction begins, and with the reconnecting
     * player's id for prompt replay.
     */
    void prompt(GameData gameData, T interaction, UUID recipientId);

    /** Validates and applies the player's answer, then advances the game (queue, stack, auto-pass). */
    void handleAnswer(GameData gameData, Player player, T interaction, InteractionAnswer answer);
}
