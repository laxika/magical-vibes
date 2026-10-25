package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GoblinGameState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves hidden-number choices for Goblin Game, Menacing Ogre, and Wheel of Misfortune. The life losses that follow
 * the reveal are replaced by Lich's Duel Mastery's sacrifice, whose death replacements are chosen before anything
 * changes (CR 616.1): the last number asks them before it is recorded, and the same number is answered again. Goblin
 * Game's "then loses half their life" is determined only after the first losses, so it is a step of its own.
 */
@Component
@RequiredArgsConstructor
public class GoblinGameEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoblinGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GoblinGameEffect goblinGameEffect = (GoblinGameEffect) effect;
        GoblinGameState state = gameData.goblinGame;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.order.addAll(apnapPlayers(gameData));
            promptNextPlayer(gameData, cardName, goblinGameEffect);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        if (state.index + 1 >= state.order.size()) {
            Map<UUID, Integer> itemCounts = new HashMap<>(state.itemCounts);
            itemCounts.put(state.currentPlayerId, gameData.chosenXValue);
            lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData,
                    revealLifeLosses(state.order, itemCounts, goblinGameEffect));
        }
        state.itemCounts.put(state.currentPlayerId, gameData.chosenXValue);
        gameData.chosenXValue = null;
        state.index++;

        if (state.index < state.order.size()) {
            promptNextPlayer(gameData, cardName, goblinGameEffect);
            return;
        }

        finish(gameData, state, cardName, entry, goblinGameEffect);
    }

    private void promptNextPlayer(GameData gameData, String cardName, GoblinGameEffect effect) {
        GoblinGameState state = gameData.goblinGame;
        UUID playerId = state.order.get(state.index);
        state.currentPlayerId = playerId;
        boolean allowsZero = effect.highestNumberWins() || effect.discardNonLowestAndDrawSeven();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                playerId, allowsZero ? 0 : 1, Integer.MAX_VALUE,
                allowsZero
                        ? "Choose a number for " + cardName + "."
                        : "Choose how many items to hide for " + cardName + ".",
                cardName));
    }

    private void finish(GameData gameData, GoblinGameState state, String cardName,
                        StackEntry entry, GoblinGameEffect effect) {
        boolean highestNumberWins = effect.highestNumberWins() || effect.discardNonLowestAndDrawSeven();
        int winningNumber = highestNumberWins
                ? state.itemCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0)
                : state.itemCounts.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        int lowestNumber = state.itemCounts.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        List<UUID> winningPlayers = state.order.stream()
                .filter(playerId -> state.itemCounts.get(playerId) == winningNumber)
                .toList();

        StringBuilder reveal = new StringBuilder("Players reveal their hidden numbers: ");
        for (int i = 0; i < state.order.size(); i++) {
            if (i > 0) {
                reveal.append(", ");
            }
            UUID playerId = state.order.get(i);
            reveal.append(gameData.playerIdToName.get(playerId))
                    .append(" reveals ")
                    .append(state.itemCounts.get(playerId));
        }
        gameLogService.append(gameData, GameLog.text(reveal + "."));

        if (effect.highestNumberWins()) {
            lifeSupport.applyLifeLosses(gameData, revealLifeLosses(state.order, state.itemCounts, effect), cardName);

            if (effect.countersOnSourceIfControllerWins() > 0
                    && winningPlayers.contains(entry.getControllerId())) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source != null) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, entry, source,
                            CounterType.PLUS_ONE_PLUS_ONE,
                            effect.countersOnSourceIfControllerWins());
                }
            }
        } else if (effect.discardNonLowestAndDrawSeven()) {
            if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
                int damage = winningNumber;
                for (UUID playerId : winningPlayers) {
                    damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
                }
                gameOutcomeService.checkWinCondition(gameData);
            }

            for (UUID playerId : state.order) {
                if (state.itemCounts.get(playerId) != lowestNumber) {
                    discardHandEffectHandler.discardHand(gameData, playerId, entry.getControllerId(), cardName);
                    playerInteractionSupport.applyDrawCards(gameData, playerId, 7);
                }
            }
        } else {
            lifeSupport.applyLifeLosses(gameData, revealLifeLosses(state.order, state.itemCounts, effect), cardName);

            Map<UUID, Integer> halfLifeLosses = new LinkedHashMap<>();
            for (UUID playerId : winningPlayers) {
                int currentLife = gameData.getLife(playerId);
                int halfLife = currentLife > 0 ? (currentLife + 1) / 2 : 0;
                if (halfLife > 0) {
                    halfLifeLosses.put(playerId, halfLife);
                }
            }
            if (!halfLifeLosses.isEmpty()) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                        List.of(new PlayersLoseLifeEffect(halfLifeLosses)));
            }
        }

        state.reset();
    }

    /**
     * The life each player loses once the numbers are revealed: the highest number's players lose it (Menacing Ogre),
     * or each player loses their own number (Goblin Game). Wheel of Misfortune deals damage instead.
     */
    private static Map<UUID, Integer> revealLifeLosses(List<UUID> order, Map<UUID, Integer> itemCounts,
                                                       GoblinGameEffect effect) {
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        if (effect.discardNonLowestAndDrawSeven()) {
            return losses;
        }
        if (effect.highestNumberWins()) {
            int highest = itemCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            for (UUID playerId : order) {
                if (itemCounts.get(playerId) == highest) {
                    losses.put(playerId, highest);
                }
            }
            return losses;
        }
        for (UUID playerId : order) {
            losses.put(playerId, itemCounts.get(playerId));
        }
        return losses;
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }
}
