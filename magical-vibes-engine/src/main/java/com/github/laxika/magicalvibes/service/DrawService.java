package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnFromGraveyardInsteadOfDrawEffect;
import com.github.laxika.magicalvibes.model.effect.BoobyTrapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOpponentPermanentOnDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawDrawOnBasicLandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ZursWeirdingDrawReplacementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DrawService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    // @Lazy to break the constructor cycle DrawService → InteractionHandlerRegistry →
    // (graveyard/card choice handlers) → DrawService.
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public DrawService(GameQueryService gameQueryService,
                       GameBroadcastService gameBroadcastService,
                       GameOutcomeService gameOutcomeService,
                       TriggeredAbilityQueueService triggeredAbilityQueueService,
                       @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.gameOutcomeService = gameOutcomeService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    public void resolveDrawCard(GameData gameData, UUID playerId) {
        if (isDrawPrevented(gameData)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " can't draw a card."));
            log.info("Game {} - {} can't draw (draw prevention in effect)", gameData.id, playerName);
            return;
        }

        // Forbidden Crypt — "If you would draw a card, return a card from your graveyard to your
        // hand instead. If you can't, you lose the game." Mandatory replacement for the drawer.
        if (findReturnFromGraveyardInsteadOfDrawSourceCard(gameData, playerId) != null) {
            // A prior forced return from the same multi-card draw is still awaiting a choice; don't
            // stack another interaction over it (it would overwrite the active one).
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                // Can't return a card — the player loses the game (CR 104.3a, replacement wording).
                if (gameQueryService.canPlayerLoseGame(gameData, playerId)
                        && !gameOutcomeService.replaceLossWithGameReset(gameData, playerId)) {
                    UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                    String lossLog = gameData.playerIdToName.get(playerId)
                            + " can't return a card from their graveyard and loses the game.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(lossLog));
                    log.info("Game {} - {} loses (Forbidden Crypt: empty graveyard on draw)",
                            gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, winnerId);
                }
                return;
            }
            List<Integer> validIndices = IntStream.range(0, graveyard.size()).boxed().toList();
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                    .builder(playerId, validIndices, GraveyardChoiceDestination.HAND,
                            "Return a card from your graveyard to your hand.")
                    .build());
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

        Card zursWeirdingSource = findZursWeirdingSourceCard(gameData);
        if (zursWeirdingSource != null) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null && !deck.isEmpty()) {
                UUID otherPlayerId = gameQueryService.getOpponentId(gameData, playerId);
                Card revealed = deck.getFirst();
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + revealed.getName() + " with " + zursWeirdingSource.getName() + "."));

                if (gameData.getLife(otherPlayerId) >= 2) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            zursWeirdingSource,
                            otherPlayerId,
                            List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ZURS_WEIRDING)),
                            "Pay 2 life to put " + playerName + "'s revealed " + revealed.getName() + " into their graveyard?"
                    ));
                    return;
                }
            }
            performDrawCard(gameData, playerId);
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);
            performDrawCard(gameData, replacementController);
            return;
        }

        // Thought Reflection: if you would draw a card, draw two cards instead.
        if (findDoubleDrawSourceCard(gameData, playerId) != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s draw is doubled — they draw two cards instead."));
            log.info("Game {} - {}'s draw doubled (Thought Reflection)", gameData.id, playerName);
            performDrawCard(gameData, playerId);
            performDrawCard(gameData, playerId);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    public void resolveDrawCardWithoutStaticReplacementCheck(GameData gameData, UUID playerId) {
        if (isDrawPrevented(gameData)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " can't draw a card."));
            log.info("Game {} - {} can't draw (draw prevention in effect)", gameData.id, playerName);
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

    private Card findZursWeirdingSourceCard(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof ZursWeirdingDrawReplacementEffect);
                if (hasEffect) {
                    return permanent.getCard();
                }
            }
        }
        return null;
    }

    private Card findDoubleDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof DoubleDrawReplacementEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Card findReturnFromGraveyardInsteadOfDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof ReturnFromGraveyardInsteadOfDrawEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

            // Check for Laboratory Maniac-style replacement: win instead of lose
            if (hasWinOnEmptyLibraryDraw(gameData, playerId)) {
                UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
                if (gameQueryService.canPlayerLoseGame(gameData, opponentId)) {
                    String winLog = gameData.playerIdToName.get(playerId) + " wins the game (drew from an empty library with a replacement effect).";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(winLog));
                    log.info("Game {} - {} wins (empty library draw replacement)", gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, playerId);
                } else {
                    String blockedLog = gameData.playerIdToName.get(playerId) + "'s win condition is met but " +
                            gameData.playerIdToName.get(opponentId) + " can't lose the game.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(blockedLog));
                    log.info("Game {} - {} empty library win prevented — opponent can't lose", gameData.id, gameData.playerIdToName.get(playerId));
                }
                return;
            }

            // CR 704.5b — player who attempted to draw from an empty library loses the game
            if (gameQueryService.canPlayerLoseGame(gameData, playerId)
                    && !gameOutcomeService.replaceLossWithGameReset(gameData, playerId)) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String lossLog = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(lossLog));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));

        checkControllerDrawTriggers(gameData, playerId);
        checkOpponentDrawTriggers(gameData, playerId);
        checkBoobyTraps(gameData, playerId, drawn);
        checkRevealFirstDrawTriggers(gameData, playerId, drawn);
    }

    /**
     * Rowen: the controller reveals the first card they draw each turn; whenever that card is a basic
     * land, a "draw a card" triggered ability is put onto the stack. Only the turn's first draw is
     * revealed — {@code cardsDrawnThisTurn} has already been incremented for this draw, so first draw
     * means a count of exactly 1. The extra draw is therefore never revealed itself.
     */
    private void checkRevealFirstDrawTriggers(GameData gameData, UUID drawingPlayerId, Card drawn) {
        if (gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) != 1) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            boolean reveals = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof RevealFirstDrawDrawOnBasicLandEffect);
            if (!reveals) continue;

            String drawerName = gameData.playerIdToName.get(drawingPlayerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(drawerName + " reveals " + drawn.getName() + " with " + perm.getCard().getName() + "."));

            boolean basicLand = drawn.hasType(CardType.LAND)
                    && drawn.getSupertypes().contains(CardSupertype.BASIC);
            if (basicLand) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        drawingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DrawCardEffect(1))),
                        drawingPlayerId,
                        perm.getId()
                ));
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " triggers — draw a card."));
                log.info("Game {} - {} triggers on {} revealing a basic land",
                        gameData.id, perm.getCard().getName(), drawerName);
            }
        }
    }

    /**
     * Booby Trap: an opponent's Booby Trap makes the drawing (chosen) player reveal each card they
     * draw; when the revealed card's name matches the trap's chosen name, the trap is sacrificed and
     * — if it was — deals 10 damage to that player. The chosen player is always an opponent of the
     * trap's controller.
     */
    private void checkBoobyTraps(GameData gameData, UUID drawingPlayerId, Card drawn) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(drawingPlayerId)) return;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                boolean isBoobyTrap = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof BoobyTrapEffect);
                if (!isBoobyTrap) continue;

                String drawerName = gameData.playerIdToName.get(drawingPlayerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(drawerName + " reveals " + drawn.getName() + " with " + perm.getCard().getName() + "."));

                if (drawn.getName().equals(perm.getChosenName())) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(new SacrificeSelfThenDealDamageToTargetPlayerEffect(10))),
                            drawingPlayerId,
                            perm.getId()
                    ));
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " triggers on " + drawerName + " drawing " + drawn.getName() + "."));
                    log.info("Game {} - Booby Trap triggers on {} drawing {}",
                            gameData.id, drawerName, drawn.getName());
                }
            }
        });
    }

    public void checkControllerDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DRAWS);
            if (drawEffects == null || drawEffects.isEmpty()) continue;

            for (CardEffect effect : drawEffects) {
                // Equipment-granted draw trigger (Diviner's Wand): the ability is granted to the
                // equipped creature, so an unattached Equipment has no such ability — no trigger.
                if (effect instanceof BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect
                        && perm.getAttachedTo() == null) {
                    continue;
                }

                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), drawingPlayerId, may);
                } else if (effect.targetSpec().category() == TargetCategory.ANY_TARGET) {
                    // Any-target draw trigger (Niv-Mizzet, the Firemind): the controller must choose a
                    // target before the ability goes on the stack.
                    gameData.queueInteraction(new PermanentChoiceContext.DrawTriggerAnyTarget(
                            perm.getCard(),
                            drawingPlayerId,
                            new ArrayList<>(List.of(effect)),
                            perm.getId()
                    ));

                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                    log.info("Game {} - {} controller-draw any-target trigger queued",
                            gameData.id, perm.getCard().getName());
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

                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                    log.info("Game {} - {} controller-draw trigger pushed onto stack",
                            gameData.id, perm.getCard().getName());
                }
            }
        }

        // Emblem draw triggers (e.g. Teferi, Hero of Dominaria emblem)
        checkEmblemDrawTriggers(gameData, drawingPlayerId);
    }

    private void checkEmblemDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(drawingPlayerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOpponentPermanentOnDrawEffect) {
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Teferi's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard(),
                            true
                    ));
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} triggers on opponent draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }
}
