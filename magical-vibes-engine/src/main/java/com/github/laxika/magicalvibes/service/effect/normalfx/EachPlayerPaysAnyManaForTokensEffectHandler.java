package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerPayManaState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaForTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerPaysAnyManaForTokensEffect} (Liege of the Hollows): each player may
 * pay any amount of mana, then each creates one token per mana they paid.
 *
 * <p>Players are prompted once each in APNAP order (CR 101.4). The flow is driven one player at a
 * time and re-runs on every X-value choice. Each prompt is a mana-payment
 * {@link PendingInteraction.XValueChoice} capped by that player's potential mana, so they may tap
 * sources while the prompt is open; the pool is re-checked before charging. Progress lives on
 * {@link GameData#eachPlayerPayMana}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerPaysAnyManaForTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerPaysAnyManaForTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        EachPlayerPaysAnyManaForTokensEffect payEffect = (EachPlayerPaysAnyManaForTokensEffect) effect;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.sourceSetCode = entry.getCard().getSetCode();
            seedApnapOrder(gameData, state);
            promptOrFinish(gameData, payEffect, cardName);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        UUID playerId = state.currentPlayerId;
        String playerName = gameData.playerIdToName.get(playerId);

        if (amount > 0) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (payableFromPool(pool) < amount) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + amount + "} for " + cardName
                                + " (tap mana sources, then choose again)."));
                promptPlayer(gameData, state, playerId, cardName);
                return;
            }
            new ManaCost("{0}").pay(pool, amount);
            state.manaPaid.merge(playerId, amount, Integer::sum);
            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + amount + "} for " + cardName + "."));
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no mana for " + cardName + "."));
        }
        state.index++;
        promptOrFinish(gameData, payEffect, cardName);
    }

    /** Active player first, then the remaining players in turn order (CR 101.4). */
    private static void seedApnapOrder(GameData gameData, EachPlayerPayManaState state) {
        int size = gameData.orderedPlayerIds.size();
        int start = Math.max(0, gameData.orderedPlayerIds.indexOf(gameData.activePlayerId));
        for (int i = 0; i < size; i++) {
            UUID playerId = gameData.orderedPlayerIds.get((start + i) % size);
            state.order.add(playerId);
            state.manaPaid.put(playerId, 0);
        }
    }

    /**
     * Prompts the next player who has mana available, skipping any player who can't pay anything.
     * Once everyone has chosen, finishes by creating each player's tokens and clearing the flow.
     */
    private void promptOrFinish(GameData gameData, EachPlayerPaysAnyManaForTokensEffect effect, String cardName) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        while (state.index < state.order.size()) {
            UUID playerId = state.order.get(state.index);
            if (maxPotentialX(gameData, playerId) <= 0) {
                state.index++;
                continue;
            }
            promptPlayer(gameData, state, playerId, cardName);
            return;
        }
        finish(gameData, effect, cardName);
    }

    private void promptPlayer(GameData gameData, EachPlayerPayManaState state, UUID playerId, String cardName) {
        state.currentPlayerId = playerId;
        String prompt = "Pay any amount of mana for " + cardName
                + ". You will create a 1/1 Squirrel token for each mana paid.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, maxPotentialX(gameData, playerId), prompt, cardName, true));
    }

    /** Creates each player's tokens (one per mana paid) and clears the flow. */
    private void finish(GameData gameData, EachPlayerPaysAnyManaForTokensEffect effect, String cardName) {
        EachPlayerPayManaState state = gameData.eachPlayerPayMana;
        for (UUID playerId : state.order) {
            int count = state.manaPaid.getOrDefault(playerId, 0);
            for (int i = 0; i < count; i++) {
                destructionSupport.createTokenForPlayer(gameData, playerId, effect.token(), cardName,
                        state.sourceSetCode);
            }
        }
        state.reset();
    }

    /** Pool mana plus untapped sources — CR 605.3a lets players tap during the payment. */
    private int maxPotentialX(GameData gameData, UUID playerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal()
                - gameData.playerManaPools.get(playerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(playerId)) + untappedSources;
    }

    /** Generic-payable mana in the pool right now — mirrors what {@code pay} can drain. */
    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }
}
