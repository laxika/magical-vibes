package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerPayLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyLifeForTokensEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerPaysAnyLifeForTokensEffect} (Plague of Vermin): starting with the
 * controller, each player may pay any amount of life; this repeats round-robin until a full round
 * passes with no one paying; then each player creates one token for each 1 life they paid.
 *
 * <p>The flow is driven one player at a time and re-runs on every X-value choice (the engine
 * re-runs the current effect while an X-value choice is active). Each prompt is an
 * {@link PendingInteraction.XValueChoice} for the amount of life (0 through the chooser's current
 * life total). Progress lives on {@link GameData#eachPlayerPayLife}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyLifeForTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyLifeForTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayLifeState state = gameData.eachPlayerPayLife;
        EachPlayerPaysAnyLifeForTokensEffect payEffect = (EachPlayerPaysAnyLifeForTokensEffect) effect;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            // Fresh entry: seed the round-robin (controller first, then remaining players in turn order).
            state.reset();
            state.active = true;
            UUID controllerId = entry.getControllerId();
            state.order.add(controllerId);
            state.lifePaid.put(controllerId, 0);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(controllerId)) {
                    state.order.add(playerId);
                    state.lifePaid.put(playerId, 0);
                }
            }
            promptOrFinish(gameData, payEffect, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            int amount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = state.currentPlayerId;
            String playerName = gameData.playerIdToName.get(playerId);

            if (amount > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, amount, cardName);
                state.lifePaid.merge(playerId, amount, Integer::sum);
                state.consecutivePasses = 0;
            } else {
                gameBroadcastService.logAndBroadcast(gameData, playerName + " pays no life for " + cardName + ".");
                state.consecutivePasses++;
            }
            state.index = (state.index + 1) % state.order.size();
            promptOrFinish(gameData, payEffect, cardName);
        }
    }

    /**
     * Prompts the next player who can pay life, skipping (as an automatic pass) any player whose
     * life total is 0 or less. When a full round of consecutive passes has elapsed, finishes by
     * creating each player's tokens and clearing the flow.
     */
    private void promptOrFinish(GameData gameData, EachPlayerPaysAnyLifeForTokensEffect effect, String cardName) {
        EachPlayerPayLifeState state = gameData.eachPlayerPayLife;
        while (state.consecutivePasses < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            int life = gameData.getLife(playerId);
            if (life <= 0) {
                state.consecutivePasses++;
                state.index = (state.index + 1) % state.order.size();
                continue;
            }
            state.currentPlayerId = playerId;
            String prompt = "Pay any amount of life for " + cardName
                    + ". You will create a 1/1 Rat token for each 1 life paid.";
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.XValueChoice(playerId, life, prompt, cardName));
            return;
        }
        finish(gameData, effect, cardName);
    }

    /** Creates each player's Rat tokens (one per life paid) and clears the flow. */
    private void finish(GameData gameData, EachPlayerPaysAnyLifeForTokensEffect effect, String cardName) {
        EachPlayerPayLifeState state = gameData.eachPlayerPayLife;
        for (UUID playerId : state.order) {
            int count = state.lifePaid.getOrDefault(playerId, 0);
            for (int i = 0; i < count; i++) {
                destructionSupport.createTokenForPlayer(gameData, playerId, effect.token(), cardName);
            }
        }
        state.reset();
    }
}
