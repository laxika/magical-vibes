package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    public void resolveDrawCard(GameData gameData, UUID playerId) {
        if (isDrawPrevented(gameData)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " can't draw a card.");
            log.info("Game {} - {} can't draw (draw prevention in effect)", gameData.id, playerName);
            return;
        }

        Card abundanceSource = findAbundanceSourceCard(gameData, playerId);
        if (abundanceSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    abundanceSource,
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ABUNDANCE)),
                    "Replace this draw with Abundance?"
            ));
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);
            performDrawCard(gameData, replacementController);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    public void resolveDrawCardWithoutStaticReplacementCheck(GameData gameData, UUID playerId) {
        if (isDrawPrevented(gameData)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " can't draw a card.");
            log.info("Game {} - {} can't draw (draw prevention in effect)", gameData.id, playerName);
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);

            if (replacementController.equals(playerId)) {
                performDrawCard(gameData, replacementController);
            } else {
                resolveDrawCard(gameData, replacementController);
            }
            return;
        }

        performDrawCard(gameData, playerId);
    }

    private boolean isDrawPrevented(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                boolean prevents = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PlayersCannotDrawCardsEffect);
                if (prevents) return true;
            }
        }
        return false;
    }

    private Card findAbundanceSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasAbundanceEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof AbundanceDrawReplacementEffect);
            if (hasAbundanceEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    void performDrawCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameData.playersAttemptedDrawFromEmptyLibrary.add(playerId);
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            // Check for Laboratory Maniac-style replacement: win instead of lose
            if (hasWinOnEmptyLibraryDraw(gameData, playerId)) {
                UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
                if (gameQueryService.canPlayerLoseGame(gameData, opponentId)) {
                    String winLog = gameData.playerIdToName.get(playerId) + " wins the game (drew from an empty library with a replacement effect).";
                    gameBroadcastService.logAndBroadcast(gameData, winLog);
                    log.info("Game {} - {} wins (empty library draw replacement)", gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, playerId);
                } else {
                    String blockedLog = gameData.playerIdToName.get(playerId) + "'s win condition is met but " +
                            gameData.playerIdToName.get(opponentId) + " can't lose the game.";
                    gameBroadcastService.logAndBroadcast(gameData, blockedLog);
                    log.info("Game {} - {} empty library win prevented — opponent can't lose", gameData.id, gameData.playerIdToName.get(playerId));
                }
                return;
            }

            // CR 704.5b — player who attempted to draw from an empty library loses the game
            if (gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String lossLog = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                gameBroadcastService.logAndBroadcast(gameData, lossLog);
                log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                gameOutcomeService.declareWinner(gameData, winnerId);
            }
            return;
        }

        Card drawn = deck.removeFirst();
        gameData.addCardToHand(playerId, drawn);

        // Track cards drawn this turn (for Molten Psyche, etc.)
        gameData.cardsDrawnThisTurn.merge(playerId, 1, Integer::sum);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));

        checkControllerDrawTriggers(gameData, playerId);
        checkOpponentDrawTriggers(gameData, playerId);
    }

    public void checkControllerDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DRAWS);
            if (drawEffects == null || drawEffects.isEmpty()) continue;

            for (CardEffect effect : drawEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), drawingPlayerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            drawingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            drawingPlayerId,
                            perm.getId()
                    ));

                    String triggerLog = perm.getCard().getName() + " triggers — each opponent loses life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} controller-draw trigger pushed onto stack",
                            gameData.id, perm.getCard().getName());
                }
            }
        }
    }

    private boolean hasWinOnEmptyLibraryDraw(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof WinGameOnEmptyLibraryDrawEffect);
            if (hasEffect) return true;
        }
        return false;
    }

    public void checkOpponentDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(drawingPlayerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DRAWS);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                drawingPlayerId,
                                perm.getId()
                        ));
                    }

                    String logEntry = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on opponent draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }
}
