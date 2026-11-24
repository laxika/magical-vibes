package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.DiscardToTopOfLibraryInsteadEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Shared draw/discard/reveal/choice helpers used by every PlayerInteraction effect handler
 * and by input services (e.g. CardChoiceHandlerService).
 *
 * <p>Extracted verbatim from PlayerInteractionResolutionService; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInteractionSupport {

    private final DrawService drawService;
    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final CardRevealService cardRevealService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void applyOpponentMayPlayCreature(GameData gameData, UUID controllerId) {

        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        resolvePlayerMayPlayCreature(gameData, opponentId);
    
    }

    public void applyOpponentMayPlayCreature(GameData gameData, UUID controllerId,
                                             OpponentMayPlayCreatureEffect effect) {

        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (effect.predicate() == null) {
            resolvePlayerMayPlayCreature(gameData, opponentId);
        } else {
            resolvePlayerMayPlayCreature(gameData, opponentId, effect.predicate(), effect.label());
        }
    }
    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect) {
        applyPutCardToBattlefield(gameData, playerId, effect, 0, null);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue,
                                          UUID sourceEquipmentCardId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, sourceEquipmentCardId, null);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
                                          int xValue, Integer maxManaValue, UUID sourceEquipmentCardId,
                                          UUID sourceCardId, UUID blockingAttackerId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, maxManaValue, sourceEquipmentCardId,
                sourceCardId, null, null, blockingAttackerId, ignored -> true, null);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue,
                                          UUID sourceEquipmentCardId, UUID sourceCardId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, sourceEquipmentCardId, sourceCardId,
                null, null, null, ignored -> true);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue,
                                          UUID sourceEquipmentCardId, UUID sourceCardId,
                                          Predicate<Card> additionalFilter) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, sourceEquipmentCardId, sourceCardId,
                null, null, null, additionalFilter);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
                                          int xValue, UUID sourceEquipmentCardId, UUID sourceCardId,
                                          CardEffect thenEffect, CardPredicate thenCondition) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, sourceEquipmentCardId, sourceCardId,
                thenEffect, thenCondition, null, ignored -> true);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue,
                                          UUID sourceEquipmentCardId, UUID sourceCardId, UUID blockingAttackerId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, sourceEquipmentCardId, sourceCardId,
                null, null, blockingAttackerId, ignored -> true);
    }

    private void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
                                           int xValue, UUID sourceEquipmentCardId, UUID sourceCardId,
                                           CardEffect thenEffect, CardPredicate thenCondition,
                                           UUID blockingAttackerId, Predicate<Card> additionalFilter) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, null, sourceEquipmentCardId, sourceCardId,
                thenEffect, thenCondition, blockingAttackerId, additionalFilter, null);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
                                           int xValue, Integer maxManaValue, UUID sourceEquipmentCardId,
                                           UUID sourceCardId, CardEffect thenEffect, CardPredicate thenCondition,
                                           UUID blockingAttackerId, Predicate<Card> additionalFilter, UUID sourcePermanentId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, maxManaValue, sourceEquipmentCardId,
                sourceCardId, thenEffect, thenCondition, blockingAttackerId, additionalFilter, sourcePermanentId, null);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
                                           int xValue, Integer maxManaValue, UUID sourceEquipmentCardId,
                                           UUID sourceCardId, CardEffect thenEffect, CardPredicate thenCondition,
                                           UUID blockingAttackerId, Predicate<Card> additionalFilter, UUID sourcePermanentId,
                                           Permanent sourcePermanentSnapshot) {

        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card handCard = hand.get(i);
                boolean matches = sourcePermanentSnapshot == null
                        ? predicateEvaluationService.matchesCardPredicate(
                                handCard, effect.predicate(), sourceCardId, gameData, playerId)
                        : predicateEvaluationService.matchesCardPredicate(handCard, effect.predicate(), sourceCardId,
                                gameData, playerId, sourcePermanentId, null, xValue, sourcePermanentSnapshot);
                if (!matches) {
                    continue;
                }
                if (!additionalFilter.test(handCard)) {
                    continue;
                }
                if (gameData.heldHandBattlefieldPuts.stream()
                        .anyMatch(held -> held.card().getId().equals(handCard.getId()))) {
                    continue;
                }
                // Mind into Matter: "mana value X or less".
                if (effect.maxManaValueBoundedByX() && handCard.getManaValue() > xValue) {
                    continue;
                }
                if (effect.maxManaValue() != null
                        && (maxManaValue == null || handCard.getManaValue() > maxManaValue)) {
                    continue;
                }
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.putsNoCardOntoBattlefield(playerName));
            log.info("Game {} - {} has no {} cards in hand for hand-to-battlefield effect", gameData.id, playerName, effect.label());
            return;
        }

        String tappedSuffix = effect.enterTapped() && effect.enterAttacking() ? " tapped and attacking"
                : effect.enterTapped() ? " tapped"
                : effect.enterAttacking() ? " attacking"
                : "";
        if (effect.enterBlocking()) {
            tappedSuffix += " blocking that creature";
        }
        boolean repeats = effect.drawAndRepeat() || effect.putAnyNumber();
        String prompt = effect.cloaked()
                ? "Choose a card from your hand to cloak."
                : effect.drawAndRepeat()
                ? "You may put a " + effect.label() + " card from your hand onto the battlefield" + tappedSuffix
                + ". If you do, draw a card and repeat this process."
                : effect.putAnyNumber()
                ? "Choose a " + effect.label() + " card from your hand to put onto the battlefield" + tappedSuffix
                + " (or decline to finish)."
                : "Choose a " + effect.label() + " card from your hand to put onto the battlefield" + tappedSuffix + ".";
        UUID attachEquipmentCardId = effect.attachSourceEquipment() ? sourcePermanentId : null;
        UUID returnExiledSourceCardId = effect.returnExiledSourceIfSacrificed()
                && gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry.getCard().getId() : null;
        boolean persistentHaste = effect.grantHaste();
        if (effect.grantHaste() && effect.hasteUntilEndOfTurn() && thenEffect == null) {
            persistentHaste = false;
            thenEffect = new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.HASTE, null);
            thenCondition = null;
        }
        playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt, effect.enterTapped(),
                persistentHaste, effect.sacrificeAtEndStep(), attachEquipmentCardId, effect.enterAttacking(),
                effect.drawAndRepeat(), repeats ? effect.predicate() : null,
                repeats ? effect.label() : null, effect.putAnyNumber(), effect.faceDown(),
                effect.faceDownPower(), effect.faceDownToughness(), effect.faceDownCardTypes(),
                returnExiledSourceCardId, effect.returnToHandAtEndStep(), effect.cloaked(),
                thenEffect, thenCondition, effect.enterTappedAndAttackingIf(), blockingAttackerId,
                sourcePermanentId, effect.untapSourceIfEnteredCardHasAnySubtype(),
                // The entered permanent remembers the permanent whose ability put it there ("if it wasn't put onto
                // the battlefield with this ability").
                sourcePermanentId != null,
                // "Put this card from your hand onto the battlefield" is an instruction, not a "you may" choice.
                effect.predicate() instanceof CardIsSelfPredicate);

    }
    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect,
            int xValue, UUID sourceEquipmentCardId, UUID sourceCardId,
            CardEffect thenEffect, CardPredicate thenCondition, UUID blockingAttackerId,
            Predicate<Card> additionalFilter, UUID sourcePermanentId) {
        applyPutCardToBattlefield(gameData, playerId, effect, xValue, null, sourceEquipmentCardId,
                sourceCardId, thenEffect, thenCondition, blockingAttackerId, additionalFilter, sourcePermanentId);
    }

    public void resolvePlayerMayPlayCreature(GameData gameData, UUID playerId) {

        List<Card> hand = gameData.playerHands.get(playerId);

        List<Integer> creatureIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).hasType(CardType.CREATURE)) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.putsNoCardOntoBattlefield(playerName));
            log.info("Game {} - {} has no creatures in hand for creature-choice effect", gameData.id, playerName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, creatureIndices, prompt);
    
    }

    private void resolvePlayerMayPlayCreature(GameData gameData, UUID playerId,
                                              CardPredicate predicate, String label) {

        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card handCard = hand.get(i);
                if (predicateEvaluationService.matchesCardPredicate(handCard, predicate, handCard.getId())) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.putsNoCardOntoBattlefield(playerName));
            log.info("Game {} - {} has no {} cards in hand for creature-choice effect", gameData.id, playerName, label);
            return;
        }

        String prompt = "You may put a " + label + " card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt);
    }
    public void applyDrawCards(GameData gameData, UUID playerId, int amount) {
        drawService.resolveDrawCards(gameData, playerId, amount);
    }
    /**
     * Sindbad: the player draws a card and reveals it; if the revealed card isn't a land card, it is
     * discarded. The reveal is public (logged); the discard is not opponent-caused. The freshly drawn
     * card is the last card appended to the hand, so nothing is discarded when the draw produced no
     * card (empty library) or was consumed by a draw-replacement interaction.
     */
    public void applyDrawRevealDiscardUnlessLand(GameData gameData, UUID playerId) {
        Integer beforeDrawCount = gameData.pendingDrawRevealDiscardDrawCounts.remove(playerId);
        if (beforeDrawCount == null) {
            beforeDrawCount = gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of()).size();
            gameData.pendingDrawRevealDiscardDrawCounts.put(playerId, beforeDrawCount);
            gameData.rerunCurrentEffectAfterInteraction = true;
            applyDrawCards(gameData, playerId, 1);
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                return;
            }
            gameData.pendingDrawRevealDiscardDrawCounts.remove(playerId);
        }
        gameData.rerunCurrentEffectAfterInteraction = false;

        List<UUID> drawnCardIds = gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of());
        // CR 121.6c: a replaced draw (nothing drawn, or several cards drawn instead) gets no reveal or discard.
        if (drawnCardIds.size() - beforeDrawCount != 1) {
            return;
        }

        UUID drawnCardId = drawnCardIds.getLast();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card drawn = hand == null ? null : hand.stream()
                .filter(card -> card.getId().equals(drawnCardId))
                .findFirst()
                .orElse(null);
        if (drawn == null) {
            return;
        }
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals ", drawn, "."));

        if (drawn.hasType(CardType.LAND)) {
            return;
        }

        hand.remove(drawn);
        gameData.discardCausedByOpponent = false;
        graveyardService.discardCard(gameData, playerId, drawn);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " discards ", drawn, "."));
        gameData.discarded(playerId, drawn);

    }
    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        resolveDiscardCards(gameData, playerId, amount, DiscardFollowUp.NONE);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount, DiscardFollowUp followUp) {
        resolveDiscardCards(gameData, playerId, amount, followUp, (CardType) null);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                    DiscardFollowUp followUp, CardType stopAfterDiscardingType) {

        resolveDiscardCards(gameData, playerId, amount, followUp, stopAfterDiscardingType, null);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                    DiscardFollowUp followUp, CardPredicate stopAfterDiscardingPredicate) {

        resolveDiscardCards(gameData, playerId, amount, followUp, null, stopAfterDiscardingPredicate);
    }

    private void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                     DiscardFollowUp followUp, CardType stopAfterDiscardingType,
                                     CardPredicate stopAfterDiscardingPredicate) {

        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        if (stopAfterDiscardingType == null && stopAfterDiscardingPredicate == null) {
            playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp);
        } else if (stopAfterDiscardingPredicate != null) {
            playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp,
                    stopAfterDiscardingPredicate);
        } else {
            playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp,
                    stopAfterDiscardingType);
        }

    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                    List<Integer> validIndices) {
        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }
        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " has no eligible cards to discard."));
            return;
        }
        playerInputService.beginDiscardChoice(gameData, playerId, validIndices,
                "Choose a card to discard.", Math.min(amount, validIndices.size()));
    }

    public void resolveRandomDiscardCards(GameData gameData, UUID playerId, String sourceName, int amount) {

        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        gameData.beginDiscardEvent(playerId);
        Card replacementSource = graveyardService.findDiscardToLibraryReplacementSource(gameData, playerId);
        List<Card> awaitingReplacementChoice = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            List<Card> candidates = gameData.playerHands.get(playerId).stream()
                    .filter(card -> !awaitingReplacementChoice.contains(card))
                    .toList();
            if (candidates.isEmpty()) break;
            Card discarded = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            if (!discardOrAskReplacement(gameData, playerId, discarded, replacementSource, " at random.")) {
                awaitingReplacementChoice.add(discarded);
            } else {
                log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            }
        }
        gameData.finishDiscardEvent();

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)

    }

    /** {@code playerId} discards exactly these cards from their hand, as one event. */
    public void discardCards(GameData gameData, UUID playerId, List<Card> cards) {
        if (cards.isEmpty()
                || gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }
        gameData.beginDiscardEvent(playerId);
        Card replacementSource = graveyardService.findDiscardToLibraryReplacementSource(gameData, playerId);
        for (Card card : cards) {
            if (gameData.playerHands.get(playerId).contains(card)) {
                discardOrAskReplacement(gameData, playerId, card, replacementSource, ".");
            }
        }
        gameData.finishDiscardEvent();
    }

    /** Discards the card; {@code false} where its owner is asked first whether it goes on top of the library. */
    private boolean discardOrAskReplacement(GameData gameData, UUID playerId, Card discarded, Card replacementSource,
                                            String logEnd) {
        boolean entersBattlefield = gameData.discardCausedByOpponent
                && discarded.getStaticEffects(Zone.HAND).stream()
                .anyMatch(EnterBattlefieldOnDiscardEffect.class::isInstance);
        if (replacementSource != null && !entersBattlefield && !discarded.isToken()) {
            // The owner chooses whether each such card goes on top of the library instead of into the graveyard;
            // the card stays in hand until that choice is made.
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    replacementSource, playerId,
                    List.of(new DiscardToTopOfLibraryInsteadEffect()),
                    "Put " + discarded.getName() + " on top of your library instead of into your graveyard?",
                    discarded.getId(), (String) null, (UUID) null));
            return false;
        }
        gameData.playerHands.get(playerId).remove(discarded);
        if (entersBattlefield) {
            battlefieldEntryService.putPermanentOntoBattlefieldFromOpponentDiscard(
                    gameData, playerId, new Permanent(discarded));
        } else {
            graveyardService.discardCard(gameData, playerId, discarded);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " discards ", discarded, logEnd));
        gameData.discarded(playerId, discarded);
        if (entersBattlefield && discarded.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, discarded, null, false);
        }
        return true;
    }

    /**
     * "Target player reveals their hand." The whole hand is revealed to all players via the game
     * log; nothing further happens (Thoughtcutter Agent).
     */
    public void resolveRevealHand(GameData gameData, UUID playerId) {
        cardRevealService.revealHandToAllPlayers(gameData, playerId);
    }

    /**
     * Struggle for Sanity: reveals the targeted player's hand and begins the alternating exile
     * (the targeted player picks first). No-op on an empty hand.
     */
    public void resolveAlternatingHandExile(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " reveals their hand. It is empty."));
            return;
        }

        GameLog.Builder revealBuilder = GameLog.builder().text(targetName + " reveals their hand: ");
        appendCardList(revealBuilder, hand);
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());
        cardRevealService.revealToAllPlayers(
                gameData, targetPlayerId, GameEventFact.RevealZone.HAND, hand);

        beginAlternatingHandExile(gameData, targetPlayerId, targetPlayerId, controllerId,
                List.of(), List.of());
    }

    /**
     * Begins the next pick of the alternating hand exile over the target's current hand, or applies
     * the two accumulated piles when the hand is empty. Returns {@code true} when a pick was begun.
     */
    public boolean beginAlternatingHandExile(GameData gameData, UUID decidingPlayerId, UUID targetPlayerId,
                                             UUID controllerId, List<UUID> targetExiledIds,
                                             List<UUID> controllerExiledIds) {
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return false;
        }
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AlternatingHandExileChoice(
                decidingPlayerId, targetPlayerId, controllerId, validIndices,
                new ArrayList<>(targetExiledIds), new ArrayList<>(controllerExiledIds)));
        return true;
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, false, 0);
    }

    /**
     * Same flow, but with {@code declineFallbackDiscardCount > 0} the pick is optional and the
     * target player discards that many cards of their own choice when the caster declines or when
     * their hand offers no legal choice (Nightsnare).
     */
    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, int declineFallbackDiscardCount) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, false, declineFallbackDiscardCount);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                0, false);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             boolean imprintOnSource) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                0, imprintOnSource);
    }

    /**
     * {@code optional} lets the caster stop early ("choose up to X cards", Reap Intellect).
     */
    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, false);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount,
                                             boolean imprintOnSource) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, true);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount,
                                             boolean imprintOnSource,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, true, false,
                grantPlayPermission, returnAtNextEndStep);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep,
                                             int exilePlayOpponentTax) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, true, false,
                grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax);
    }

    /**
     * True when the effect currently resolving is a {@link com.github.laxika.magicalvibes.model.effect.MayEffect}
     * whose handler already looked at the hand before the player decided (see
     * {@link ChooseCardsFromTargetHandEffect#looksAtHandBeforeMayChoice()}).
     */
    private boolean isResolvingRevealMatchingOnly(StackEntry entry) {
        int index = entry.getResolvingEffectIndex();
        List<CardEffect> effects = entry.getEffectsToResolve();
        return index >= 0 && index < effects.size()
                && effects.get(index) instanceof ChooseCardsFromTargetHandEffect choose
                && choose.revealMatchingOnly();
    }

    private boolean isInsideMayThatLooksAtHandFirst(StackEntry entry) {
        int index = entry.getResolvingEffectIndex();
        List<CardEffect> effects = entry.getEffectsToResolve();
        return index >= 0 && index < effects.size()
                && effects.get(index) instanceof com.github.laxika.magicalvibes.model.effect.MayEffect may
                && may.wrapped() instanceof ChooseCardsFromTargetHandEffect hand
                && hand.looksAtHandBeforeMayChoice();
    }

    public void resolveHandRevealAndChooseWithChosenCardThen(GameData gameData, StackEntry entry,
                                                              int count, List<CardType> excludedTypes,
                                                              List<CardType> includedTypes, CardPredicate filter,
                                                              boolean discardMode, boolean exileMode,
                                                              UUID sourcePermanentId, boolean optional,
                                                              int declineFallbackDiscardCount, boolean imprintOnSource,
                                                              boolean revealHand, boolean grantPlayPermission,
                                                              boolean returnAtNextEndStep, int exilePlayOpponentTax,
                                                              CardPredicate chosenCardCondition,
                                                              CardEffect chosenCardThenEffect) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, false,
                grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax,
                chosenCardCondition, chosenCardThenEffect, null, null);
    }

    public void resolveHandRevealAndChooseWithChosenCardThen(GameData gameData, StackEntry entry,
                                                              int count, List<CardType> excludedTypes,
                                                              List<CardType> includedTypes, CardPredicate filter,
                                                              boolean discardMode, boolean exileMode,
                                                              UUID sourcePermanentId, boolean optional,
                                                              int declineFallbackDiscardCount, boolean imprintOnSource,
                                                              boolean revealHand, boolean grantPlayPermission,
                                                              boolean returnAtNextEndStep, int exilePlayOpponentTax,
                                                              CardPredicate chosenCardCondition,
                                                              CardEffect chosenCardThenEffect,
                                                              boolean keepInHand) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, false,
                grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax,
                chosenCardCondition, chosenCardThenEffect, null, null, keepInHand);
    }

    public void resolveHandRevealAndChooseOrElse(GameData gameData, StackEntry entry,
                                                  int count, List<CardType> excludedTypes,
                                                  List<CardType> includedTypes, CardPredicate filter,
                                                  CardEffect elseEffect, CardEffect currentEffect) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                true, false, null, true, 0, false, true, false,
                false, false, 0, null, null, elseEffect, currentEffect);
    }

    public void resolveHandLookAndChoose(GameData gameData, StackEntry entry,
            int count, List<CardType> excludedTypes, List<CardType> includedTypes,
            CardPredicate filter, boolean discardMode, boolean exileMode,
            UUID sourcePermanentId, int declineFallbackDiscardCount) {
        resolveHandLookAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, false, declineFallbackDiscardCount);
    }

    public void resolveHandLookAndChoose(GameData gameData, StackEntry entry,
            int count, List<CardType> excludedTypes, List<CardType> includedTypes,
            CardPredicate filter, boolean discardMode, boolean exileMode,
            UUID sourcePermanentId, boolean optional, int declineFallbackDiscardCount) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional, declineFallbackDiscardCount, false, false);
    }

    public void resolveHandRevealAndChooseToShuffleIntoLibrary(GameData gameData, StackEntry entry,
                                                               int count) {
        resolveHandRevealAndChoose(gameData, entry, count, List.of(), List.of(), null,
                false, false, null, false, 0, false, true, true, false, false);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount,
                                             boolean imprintOnSource,
                                             boolean revealHand) {

        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, false, false, false);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount,
                                             boolean imprintOnSource,
                                             boolean revealHand, boolean shuffleIntoLibraryMode,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, shuffleIntoLibraryMode,
                grantPlayPermission, returnAtNextEndStep, 0);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, int declineFallbackDiscardCount,
                                             boolean imprintOnSource,
                                             boolean revealHand, boolean shuffleIntoLibraryMode,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep,
                                             int exilePlayOpponentTax) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, shuffleIntoLibraryMode,
                grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax,
                null, null, null, null);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes,
                                             List<CardType> includedTypes, CardPredicate filter,
                                             boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean revealHand, boolean shuffleIntoLibraryMode,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep,
                                             int exilePlayOpponentTax,
                                             CardPredicate chosenCardCondition,
                                             CardEffect chosenCardThenEffect,
                                             CardEffect declineEffect,
                                             CardEffect currentEffect) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                declineFallbackDiscardCount, imprintOnSource, revealHand, shuffleIntoLibraryMode,
                grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax,
                chosenCardCondition, chosenCardThenEffect, declineEffect, currentEffect, false);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes,
                                             List<CardType> includedTypes, CardPredicate filter,
                                             boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean revealHand, boolean shuffleIntoLibraryMode,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep,
                                             int exilePlayOpponentTax,
                                             CardPredicate chosenCardCondition,
                                             CardEffect chosenCardThenEffect,
                                             CardEffect declineEffect,
                                             CardEffect currentEffect,
                                             boolean keepInHand) {

        boolean effectiveOptional = optional || declineFallbackDiscardCount > 0 || declineEffect != null;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null && entry.getTargetIds() != null) {
            targetPlayerId = entry.getTargetIds().stream()
                    .filter(gameData.playerIds::contains)
                    .findFirst()
                    .orElse(null);
        }
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        // A card chosen to stay in the hand is only chosen: what happens to it is the steps after the choice.
        String action = keepInHand ? "." : " to " + (exileMode ? "exile"
                : shuffleIntoLibraryMode ? "shuffle into their library" : "discard") + ".";

        boolean alreadyLookedAtHand = !revealHand && isInsideMayThatLooksAtHandFirst(entry);

        if (hand == null || hand.isEmpty()) {
            String logEntry = revealHand
                    ? targetName + " reveals their hand. It is empty."
                    : casterName + " looks at " + targetName + "'s hand. It is empty.";
            if (!alreadyLookedAtHand) {
                gameLogService.append(gameData, GameLog.text(logEntry));
            }
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            insertDeclineEffect(entry, currentEffect, declineEffect);
            return;
        }

        boolean revealsMatchingOnly = revealHand && isResolvingRevealMatchingOnly(entry);
        if (revealHand && !revealsMatchingOnly) {
            cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        } else if (!revealsMatchingOnly && !alreadyLookedAtHand) {
            cardRevealService.lookAtHand(gameData, casterId, targetPlayerId);
        }

        if (discardMode && gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, targetPlayerId)) {
            insertDeclineEffect(entry, currentEffect, declineEffect);
            return;
        }

        // Build valid indices based on included or excluded types, then the optional predicate filter
        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card handCard = hand.get(i);
            boolean typeMatches;
            if (!includedTypes.isEmpty()) {
                // Included mode: card must match at least one included type (primary or additional)
                typeMatches = includedTypes.contains(handCard.getType())
                        || handCard.getAdditionalTypes().stream().anyMatch(includedTypes::contains);
            } else {
                typeMatches = excludedTypes.stream().noneMatch(handCard::hasType);
            }
            if (typeMatches
                    && (filter == null || predicateEvaluationService.matchesCardPredicate(
                    handCard, filter, sourceCardId, gameData, targetPlayerId, null, null, entry.getXValue()))) {
                validIndices.add(i);
            }
        }

        if (revealsMatchingOnly) {
            cardRevealService.revealMatchingHandCardsToAllPlayers(gameData, targetPlayerId,
                    validIndices.stream().map(hand::get).toList());
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices).";
            gameLogService.append(gameData, GameLog.text(noValidEntry));
            log.info("Game {} - {}'s hand has no valid choices for {}", gameData.id, targetName, casterName);
            // No legal choice means the caster doesn't choose one, so the fallback discard applies.
            if (declineFallbackDiscardCount > 0) {
                resolveDiscardCards(gameData, targetPlayerId, declineFallbackDiscardCount);
            } else {
                insertDeclineEffect(entry, currentEffect, declineEffect);
            }
            return;
        }

        int cardsToChoose = Math.min(count, validIndices.size());

        boolean choiceOptional = effectiveOptional;
        String choicePrompt;
        if (!includedTypes.isEmpty()) {
            String typeNames = includedTypes.stream()
                    .map(CardType::getDisplayName)
                    .reduce((a, b) -> a + " or " + b)
                    .orElse("card");
            choicePrompt = (choiceOptional ? "You may choose a " : "Choose a ") + typeNames.toLowerCase()
                    + " card" + action;
        } else if (shuffleIntoLibraryMode || !excludedTypes.contains(CardType.LAND)) {
            choicePrompt = (choiceOptional ? "You may choose a card" : "Choose a card") + action;
        } else {
            choicePrompt = (choiceOptional ? "You may choose a nonland card" : "Choose a nonland card") + action;
        }
        // sourcePermanentId tracks exile-until-source-leaves effects or an imprint.
        PendingInteraction.RevealedHandChoice interaction = new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, cardsToChoose, discardMode, exileMode,
                List.of(), sourcePermanentId, choicePrompt, false, effectiveOptional, false,
                declineFallbackDiscardCount, choosableFilterFor(filter, excludedTypes),
                imprintOnSource, shuffleIntoLibraryMode, false, grantPlayPermission, returnAtNextEndStep,
                exilePlayOpponentTax, false, declineEffect, chosenCardCondition, chosenCardThenEffect, 0,
                keepInHand);
        interactionHandlerRegistry.begin(gameData, interaction);

        log.info("Game {} - {} choosing {} card(s) from {}'s hand{}",
                gameData.id, casterName, cardsToChoose, targetName, action);
    
    }

    /**
     * Restriction a revealed-hand choice keeps on every follow-up pick: the caller's filter plus an
     * exclusion for each excluded card type, so "choose a nonland card" stays nonland across picks.
     */
    private static CardPredicate choosableFilterFor(CardPredicate filter, List<CardType> excludedTypes) {
        CardPredicate result = filter;
        for (CardType excludedType : excludedTypes) {
            CardPredicate notType = new CardNotPredicate(new CardTypePredicate(excludedType));
            result = result == null ? notType : new CardAllOfPredicate(List.of(result, notType));
        }
        return result;
    }

    private void insertDeclineEffect(StackEntry entry, CardEffect currentEffect, CardEffect declineEffect) {
        if (declineEffect == null || currentEffect == null) {
            return;
        }
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == currentEffect) {
                entry.insertEffectsToResolve(i + 1, List.of(declineEffect));
                return;
            }
        }
    }

    /**
     * Revealing Eye: the caster reveals the target opponent's hand and may choose a matching card
     * for that player to discard and draw.
     */
    /**
     * Begins the shared reveal-and-choose flow. The target picks which cards to reveal; if they
     * hold {@code revealCount} or fewer, their whole hand is selected and the controller's choice
     * begins immediately.
     */
    public void beginRevealCardsChooseDiscard(GameData gameData, StackEntry entry, int revealCount, int discardCount) {
        beginRevealCardsChooseDiscard(gameData, entry, revealCount, discardCount, HandChoiceDestination.DISCARD);
    }

    /**
     * As {@link #beginRevealCardsChooseDiscard(GameData, StackEntry, int, int)}, but the
     * controller's pick goes to {@code destination} â€” {@code EXILE} exiles it from the target's hand
     * instead of discarding it (Vizkopa Confessor).
     */
    public void beginRevealCardsChooseDiscard(GameData gameData, StackEntry entry, int revealCount, int discardCount,
                                              HandChoiceDestination destination) {
        beginRevealCardsChooseDiscard(gameData, entry, revealCount, discardCount, destination, null);
    }

    /**
     * Begins the reveal-and-choose flow while preserving a source permanent for a source-linked
     * exile destination.
     */
    public void beginRevealCardsChooseDiscard(GameData gameData, StackEntry entry, int revealCount, int discardCount,
                                              HandChoiceDestination destination, UUID sourcePermanentId) {

        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " reveals their hand. It is empty."));
            log.info("Game {} - {}'s hand is empty for reveal-and-discard", gameData.id, targetName);
            return;
        }

        // A discard forced by an opponent enables replacement effects (e.g. Obstinate Baloth).
        if (destination == HandChoiceDestination.DISCARD) {
            if (!gameQueryService.canEffectCauseDiscard(gameData, targetPlayerId, controllerId)) {
                gameLogService.append(gameData, GameLog.text(targetName + " can't be made to discard cards."));
                log.info("Game {} - {} can't be made to discard by reveal-and-discard", gameData.id, targetName);
                return;
            }
            gameData.discardCausedByOpponent = !controllerId.equals(targetPlayerId);
        }

        if (hand.size() <= revealCount) {
            // Whole hand is revealed â€” no choice for the target player.
            List<UUID> revealedCardIds = hand.stream().map(Card::getId).toList();
            beginRevealCardsDiscardStage(gameData, targetPlayerId, controllerId, revealedCardIds, discardCount,
                    destination, sourcePermanentId);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        // The reveal-stage interaction stashes the controller's discard count in remainingCount's
        // sibling â€” carried forward once the reveal picks complete (see the discard-stage begin).
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                targetPlayerId, targetPlayerId, controllerId, true, validIndices, revealCount,
                new ArrayList<>(), discardCount, destination, sourcePermanentId));

        log.info("Game {} - {} choosing {} cards to reveal for reveal-and-discard",
                gameData.id, targetName, revealCount);
    }

    /**
     * Reveals exactly the selected set and begins the controller's choice over it (the rest of the
     * target's hand stays hidden). For {@link HandChoiceDestination#KEEP_IN_HAND}, the reveal is
     * private and only nonland cards are offered for the follow-up free-cast choice.
     */
    public boolean beginRevealCardsDiscardStage(GameData gameData, UUID targetPlayerId,
                                                UUID controllerId, List<UUID> revealedCardIds, int discardCount,
                                                HandChoiceDestination destination) {
        return beginRevealCardsDiscardStage(gameData, targetPlayerId, controllerId, revealedCardIds, discardCount,
                destination, null);
    }

    /** Begins the controller's pick while preserving a source permanent for source-linked exile. */
    public boolean beginRevealCardsDiscardStage(GameData gameData, UUID targetPlayerId,
                                                UUID controllerId, List<UUID> revealedCardIds, int discardCount,
                                                HandChoiceDestination destination, UUID sourcePermanentId) {

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> revealedCards = revealedCardIds.stream()
                .map(id -> hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        boolean privateCastChoice = destination == HandChoiceDestination.KEEP_IN_HAND;
        if (privateCastChoice) {
            cardRevealService.revealToPlayer(gameData, targetPlayerId, GameEventFact.RevealZone.HAND,
                    revealedCards, controllerId);
        } else {
            GameLog.Builder revealBuilder = GameLog.builder().text(targetName + " reveals ");
            appendCardList(revealBuilder, revealedCards);
            revealBuilder.text(".");
            gameLogService.append(gameData, revealBuilder.build());
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < revealedCardIds.size(); i++) {
            Card card = revealedCards.size() > i ? revealedCards.get(i) : null;
            if (!privateCastChoice || (card != null && !card.hasType(CardType.LAND))) {
                validIndices.add(i);
            }
        }

        int toDiscard = Math.min(discardCount, validIndices.size());
        if (toDiscard == 0) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                controllerId, targetPlayerId, controllerId, false, validIndices, toDiscard,
                new ArrayList<>(revealedCardIds), toDiscard, destination, sourcePermanentId));
        return true;
    }

    /**
     * Begins the next discard pick over the still-revealed cards (used when the controller discards
     * more than one, e.g. Noggin Whack). Unlike {@link #beginRevealCardsDiscardStage} this does not
     * re-log the reveal â€” the cards were already revealed at the start of the discard stage.
     */
    public void beginRevealCardsDiscardStageContinuation(GameData gameData, UUID targetPlayerId,
                                                         UUID controllerId, List<UUID> revealedCardIds,
                                                         int remainingDiscards, int discardCount,
                                                         HandChoiceDestination destination) {
        beginRevealCardsDiscardStageContinuation(gameData, targetPlayerId, controllerId, revealedCardIds,
                remainingDiscards, discardCount, destination, null);
    }

    /** Continues a multi-pick controller choice while preserving source-linked exile metadata. */
    public void beginRevealCardsDiscardStageContinuation(GameData gameData, UUID targetPlayerId,
                                                         UUID controllerId, List<UUID> revealedCardIds,
                                                         int remainingDiscards, int discardCount,
                                                         HandChoiceDestination destination,
                                                         UUID sourcePermanentId) {

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < revealedCardIds.size(); i++) {
            validIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                controllerId, targetPlayerId, controllerId, false, validIndices, remainingDiscards,
                new ArrayList<>(revealedCardIds), discardCount, destination, sourcePermanentId));
    }

    public boolean sharesCardType(List<Card> cards) {

        if (cards.size() < 2) return false;
        Card first = cards.get(0);
        Card second = cards.get(1);
        // Check if any card type of the first card matches any card type of the second card
        if (first.getType() == second.getType()) return true;
        if (first.getAdditionalTypes().contains(second.getType())) return true;
        if (second.getAdditionalTypes().contains(first.getType())) return true;
        for (CardType additionalType : first.getAdditionalTypes()) {
            if (second.getAdditionalTypes().contains(additionalType)) return true;
        }
        return false;
    
    }
    public StackEntryType mapCardTypeToSpellType(Card card) {

        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    
    }
    public DiscardFollowUp startNextEachPlayerDiscard(GameData gameData, DiscardFollowUp followUp) {

        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerDiscards());
        // When present, eachPlayerAmounts holds a per-chooser amount parallel to the remaining
        // choosers (Pox); otherwise every chooser discards the shared eachPlayerAmount.
        List<Integer> amounts = new ArrayList<>(followUp.eachPlayerAmounts());
        boolean variableAmounts = !amounts.isEmpty();
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            int amount = variableAmounts ? amounts.remove(0) : followUp.eachPlayerAmount();
            if (amount <= 0) {
                continue;
            }
            gameData.discardCausedByOpponent = !nextPlayerId.equals(followUp.eachPlayerControllerId());
            if (gameData.discardCausedByOpponent
                    && gameQueryService.isDiscardPrevented(gameData, nextPlayerId)) {
                followUp = followUp.incrementEachPlayerNoDiscardCount();
                continue;
            }
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            if (hand == null || hand.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(nextPlayerId) + " has no cards to discard.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                followUp = followUp.incrementEachPlayerNoDiscardCount();
                continue;
            }
            DiscardFollowUp nextFollowUp = followUp.withRemainingEachPlayer(remaining, amounts);
            playerInputService.beginDiscardChoice(gameData, nextPlayerId, amount, nextFollowUp);
            return nextFollowUp;
        }
        return followUp;
    }

    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }
}
