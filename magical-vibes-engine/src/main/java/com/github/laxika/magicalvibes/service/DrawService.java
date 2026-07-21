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
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
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
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsCreaturesToHandDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.MiracleRevealEffect;
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
import java.util.stream.Collectors;
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

        // Aladdin's Lamp — one-shot, turn-scoped delayed replacement of this player's next draw:
        // instead look at the top X cards, put all but one on the bottom in a random order, then draw.
        Integer lookAtTopX = gameData.pendingNextDrawLookAtTop.remove(playerId);
        if (lookAtTopX != null) {
            resolveNextDrawLookAtTop(gameData, playerId, lookAtTopX);
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

        // Sages of the Anima — "If you would draw a card, instead reveal the top three cards of your
        // library. Put all creature cards revealed this way into your hand and the rest on the bottom of
        // your library in any order." Mandatory replacement for the drawing controller.
        Permanent revealCreaturesSource = findRevealCreaturesDrawReplacementSource(gameData, playerId);
        if (revealCreaturesSource != null) {
            resolveRevealCreaturesDrawReplacement(gameData, playerId, revealCreaturesSource);
            return;
        }

        Card zursWeirdingSource = findZursWeirdingSourceCard(gameData);
        if (zursWeirdingSource != null) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null && !deck.isEmpty()) {
                UUID otherPlayerId = gameQueryService.getOpponentId(gameData, playerId);
                Card revealed = deck.getFirst();
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .text(playerName + " reveals ")
                        .card(revealed)
                        .text(" with ")
                        .card(zursWeirdingSource)
                        .text(".")
                        .build());

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

    private Permanent findRevealCreaturesDrawReplacementSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RevealTopCardsCreaturesToHandDrawReplacementEffect);
            if (hasEffect) {
                return permanent;
            }
        }
        return null;
    }

    /**
     * Sages of the Anima replacement: reveal the top {@code revealCount} cards of the drawing player's
     * library, put every revealed creature card into their hand, and put the rest on the bottom of their
     * library in any order (an async {@link PendingInteraction.LibraryReorder} when two or more remain).
     *
     * <p>The draw is replaced entirely, so the revealed creatures are put into hand rather than "drawn"
     * (no draw triggers, no cards-drawn bookkeeping), and an empty library does not lose the game — the
     * player simply reveals nothing.
     */
    private void resolveRevealCreaturesDrawReplacement(GameData gameData, UUID playerId, Permanent source) {
        // A prior reveal from the same multi-card draw is still awaiting a bottom-order choice; don't
        // stack another interaction over it (consistent with Forbidden Crypt's multi-draw handling).
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        int revealCount = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(effect -> effect instanceof RevealTopCardsCreaturesToHandDrawReplacementEffect)
                .map(effect -> ((RevealTopCardsCreaturesToHandDrawReplacementEffect) effect).revealCount())
                .findFirst().orElse(0);

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        int actual = deck == null ? 0 : Math.min(revealCount, deck.size());
        if (actual == 0) {
            // Library empty — the draw is replaced, so nothing happens and the player does not lose.
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    playerName + "'s library is empty; ", source.getCard(), " reveals no cards."));
            log.info("Game {} - {} reveals no cards for {} (empty library)",
                    gameData.id, playerName, source.getCard().getName());
            return;
        }

        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < actual; i++) {
            revealed.add(deck.removeFirst());
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                playerName + " reveals " + revealedNames + " with ", source.getCard(), "."));
        log.info("Game {} - {} reveals top {} cards for {}",
                gameData.id, playerName, actual, source.getCard().getName());

        List<Card> creatures = new ArrayList<>();
        List<Card> rest = new ArrayList<>();
        for (Card card : revealed) {
            if (card.hasType(CardType.CREATURE)) {
                creatures.add(card);
            } else {
                rest.add(card);
            }
        }

        for (Card creature : creatures) {
            gameData.addCardToHand(playerId, creature);
        }
        if (!creatures.isEmpty()) {
            String creatureNames = creatures.stream().map(Card::getName).collect(Collectors.joining(", "));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " puts " + creatureNames + " into their hand."));
        }

        // Put the non-creature cards on the bottom of the library in any order.
        if (rest.size() == 1) {
            deck.add(rest.getFirst());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " puts 1 card on the bottom of their library."));
        } else if (rest.size() >= 2) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    playerId, new ArrayList<>(rest), true, playerId,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
        }
    }

    /**
     * Aladdin's Lamp replacement: look at the top X cards of the player's library, keep the chosen
     * one, put the rest on the bottom in a random order, then draw a card (the kept one). With one or
     * zero cards to look at, this is just a normal draw. Otherwise a {@link PendingInteraction.LibrarySearch}
     * lets the player pick which card to keep; the bottoming-plus-final-draw completes in
     * {@code LibraryChoiceHandlerService} (the {@code DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM} destination).
     */
    private void resolveNextDrawLookAtTop(GameData gameData, UUID playerId, int x) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            // No cards to look at — "then draw a card" from an empty library (handles the loss).
            performDrawCard(gameData, playerId);
            return;
        }

        int lookCount = Math.min(x, deck.size());
        if (lookCount <= 1) {
            // Looking at a single card (or X == 1) — nothing to put on the bottom; just draw it.
            performDrawCard(gameData, playerId);
            return;
        }

        List<Card> looked = new ArrayList<>();
        for (int i = 0; i < lookCount; i++) {
            looked.add(deck.removeFirst());
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(playerName + " looks at the top " + lookCount + " cards of their library."));
        log.info("Game {} - {} looks at top {} cards (Aladdin's Lamp)", gameData.id, playerName, lookCount);

        String prompt = "Look at the top " + lookCount + " cards. Choose one to draw; the rest go to the "
                + "bottom of your library in a random order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(playerId, looked)
                        .sourceCards(new ArrayList<>(looked))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM)
                        .build(),
                prompt,
                false));
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
        gameData.cardsDrawnThisTurnIds.computeIfAbsent(playerId, k -> new ArrayList<>()).add(drawn.getId());

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));

        checkControllerDrawTriggers(gameData, playerId);
        checkOpponentDrawTriggers(gameData, playerId);
        checkBoobyTraps(gameData, playerId, drawn);
        checkRevealFirstDrawTriggers(gameData, playerId, drawn);
        checkMiracleReveal(gameData, playerId, drawn);
    }

    /**
     * Miracle (CR 702.94a): if the drawn card has a {@link MiracleCast} option and this is the
     * first card the player has drawn this turn, offer to reveal it. Accepting queues the miracle
     * triggered ability ({@link com.github.laxika.magicalvibes.model.effect.MiracleMayCastEffect}).
     */
    private void checkMiracleReveal(GameData gameData, UUID drawingPlayerId, Card drawn) {
        if (gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) != 1) {
            return;
        }
        if (drawn.getCastingOption(MiracleCast.class).isEmpty()) {
            return;
        }

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                drawn,
                drawingPlayerId,
                List.of(new MiracleRevealEffect()),
                "Reveal " + drawn.getName() + " for its miracle ability?"
        ));
        log.info("Game {} - offering miracle reveal for {}", gameData.id, drawn.getName());
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text(drawerName + " reveals ")
                    .card(drawn)
                    .text(" with ")
                    .card(perm.getCard())
                    .text(".")
                    .build());

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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), " triggers — draw a card."));
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .text(drawerName + " reveals ")
                        .card(drawn)
                        .text(" with ")
                        .card(perm.getCard())
                        .text(".")
                        .build());

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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                            .card(perm.getCard())
                            .text(" triggers on " + drawerName + " drawing ")
                            .card(drawn)
                            .text(".")
                            .build());
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

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(perm.getCard()));
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

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(perm.getCard()));
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

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on opponent draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }
}
