package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final CardRevealService cardRevealService;
    private final TriggerCollectionService triggerCollectionService;
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

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue,
                                          UUID sourceEquipmentCardId, UUID sourceCardId) {

        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card handCard = hand.get(i);
                if (!predicateEvaluationService.matchesCardPredicate(handCard, effect.predicate(), sourceCardId,
                        gameData, playerId)) {
                    continue;
                }
                // Mind into Matter: "mana value X or less".
                if (effect.maxManaValueBoundedByX() && handCard.getManaValue() > xValue) {
                    continue;
                }
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no " + effect.label() + " cards in hand.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no {} cards in hand for hand-to-battlefield effect", gameData.id, playerName, effect.label());
            return;
        }

        String tappedSuffix = effect.enterTapped() && effect.enterAttacking() ? " tapped and attacking"
                : effect.enterTapped() ? " tapped"
                : effect.enterAttacking() ? " attacking"
                : "";
        boolean repeats = effect.drawAndRepeat() || effect.putAnyNumber();
        String prompt = effect.drawAndRepeat()
                ? "You may put a " + effect.label() + " card from your hand onto the battlefield" + tappedSuffix
                + ". If you do, draw a card and repeat this process."
                : effect.putAnyNumber()
                ? "Choose a " + effect.label() + " card from your hand to put onto the battlefield" + tappedSuffix
                + " (or decline to finish)."
                : "Choose a " + effect.label() + " card from your hand to put onto the battlefield" + tappedSuffix + ".";
        UUID attachEquipmentCardId = effect.attachSourceEquipment() ? sourceEquipmentCardId : null;
        UUID returnExiledSourceCardId = effect.returnExiledSourceIfSacrificed()
                && gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry.getCard().getId() : null;
        if (effect.returnToHandAtEndStep()) {
            playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt, effect.enterTapped(),
                    effect.grantHaste(), effect.sacrificeAtEndStep(), attachEquipmentCardId, effect.enterAttacking(),
                    effect.drawAndRepeat(), repeats ? effect.predicate() : null,
                    repeats ? effect.label() : null, effect.putAnyNumber(), effect.faceDown(),
                    effect.faceDownPower(), effect.faceDownToughness(), effect.faceDownCardTypes(),
                    returnExiledSourceCardId, true);
        } else {
            playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt, effect.enterTapped(),
                    effect.grantHaste(), effect.sacrificeAtEndStep(), attachEquipmentCardId, effect.enterAttacking(),
                    effect.drawAndRepeat(), repeats ? effect.predicate() : null,
                    repeats ? effect.label() : null, effect.putAnyNumber(), effect.faceDown(),
                    effect.faceDownPower(), effect.faceDownToughness(), effect.faceDownCardTypes(),
                    returnExiledSourceCardId);
        }

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
            String logEntry = playerName + " has no creature cards in hand.";
            gameLogService.append(gameData, GameLog.text(logEntry));
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
            String logEntry = playerName + " has no " + label + " cards in hand.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no {} cards in hand for creature-choice effect", gameData.id, playerName, label);
            return;
        }

        String prompt = "You may put a " + label + " card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt);
    }
    public void applyDrawCards(GameData gameData, UUID playerId, int amount) {

        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }

    }
    /**
     * Sindbad: the player draws a card and reveals it; if the revealed card isn't a land card, it is
     * discarded. The reveal is public (logged); the discard is not opponent-caused. The freshly drawn
     * card is the last card appended to the hand, so nothing is discarded when the draw produced no
     * card (empty library) or was consumed by a draw-replacement interaction.
     */
    public void applyDrawRevealDiscardUnlessLand(GameData gameData, UUID playerId) {

        List<Card> hand = gameData.playerHands.get(playerId);
        int before = hand == null ? 0 : hand.size();
        applyDrawCards(gameData, playerId, 1);

        hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.size() <= before) {
            return;
        }

        Card drawn = hand.get(hand.size() - 1);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals ", drawn, "."));

        if (drawn.hasType(CardType.LAND)) {
            return;
        }

        hand.remove(hand.size() - 1);
        gameData.discardCausedByOpponent = false;
        graveyardService.discardCard(gameData, playerId, drawn);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " discards ", drawn, "."));
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, drawn);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

    }
    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        resolveDiscardCards(gameData, playerId, amount, DiscardFollowUp.NONE);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount, DiscardFollowUp followUp) {
        resolveDiscardCards(gameData, playerId, amount, followUp, null);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                    DiscardFollowUp followUp, CardType stopAfterDiscardingType) {

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        if (stopAfterDiscardingType == null) {
            playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp);
        } else {
            playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp,
                    stopAfterDiscardingType);
        }

    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount,
                                    List<Integer> validIndices) {
        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " has no eligible cards to discard."));
            return;
        }
        playerInputService.beginDiscardChoice(gameData, playerId, validIndices,
                "Choose a card to discard.", Math.min(amount, validIndices.size()));
    }

    public void resolveRandomDiscardCards(GameData gameData, UUID playerId, String sourceName, int amount) {

        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (int i = 0; i < amount; i++) {
            List<Card> currentHand = gameData.playerHands.get(playerId);
            if (currentHand.isEmpty()) break;
            int randomIndex = ThreadLocalRandom.current().nextInt(currentHand.size());
            Card discarded = currentHand.remove(randomIndex);
            graveyardService.discardCard(gameData, playerId, discarded);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " discards " , discarded, " at random."));
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    
    }
    /**
     * Rag Man: target player reveals their hand, then discards a card of {@code cardType} at random.
     * The whole hand is revealed to the controller; a matching card is picked uniformly at random and
     * discarded. If the hand holds no matching card, nothing is discarded.
     */
    public void resolveRevealHandAndRandomDiscardOfType(GameData gameData, UUID playerId,
            String sourceName, CardType cardType) {

        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " reveals their hand. It is empty."));
            return;
        }

        GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals their hand: ");
        appendCardList(revealBuilder, hand);
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());
        cardRevealService.revealToAllPlayers(
                gameData, playerId, GameEventFact.RevealZone.HAND, hand);

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).hasType(cardType)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no " + cardType.getDisplayName().toLowerCase() + " card to discard."));
            return;
        }

        int chosen = matchingIndices.get(ThreadLocalRandom.current().nextInt(matchingIndices.size()));
        Card discarded = hand.remove(chosen);
        graveyardService.discardCard(gameData, playerId, discarded);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " discards ", discarded, " at random."));
        log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
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
                discardMode, exileMode, sourcePermanentId, false, false, 0);
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
                discardMode, exileMode, sourcePermanentId, false, false, declineFallbackDiscardCount);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             boolean exileAllCopiesOfChosenNames) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                exileAllCopiesOfChosenNames, 0, false);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode,
                                             UUID sourcePermanentId, boolean optional,
                                             boolean exileAllCopiesOfChosenNames, boolean imprintOnSource) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                exileAllCopiesOfChosenNames, 0, imprintOnSource);
    }

    /**
     * {@code optional} lets the caster stop early ("choose up to X cards", Reap Intellect) and
     * {@code exileAllCopiesOfChosenNames} extends the exile to every same-named card in the
     * target's hand, graveyard, and library.
     */
    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, boolean exileAllCopiesOfChosenNames,
                                             int declineFallbackDiscardCount) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional,
                exileAllCopiesOfChosenNames, declineFallbackDiscardCount, false);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, boolean exileAllCopiesOfChosenNames,
                                             int declineFallbackDiscardCount, boolean imprintOnSource) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional, exileAllCopiesOfChosenNames,
                declineFallbackDiscardCount, imprintOnSource, true);
    }

    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, boolean exileAllCopiesOfChosenNames,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep) {
        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional, exileAllCopiesOfChosenNames,
                declineFallbackDiscardCount, imprintOnSource, true, false,
                grantPlayPermission, returnAtNextEndStep);
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
                discardMode, exileMode, sourcePermanentId, optional, false,
                declineFallbackDiscardCount, false, false);
    }

    public void resolveHandRevealAndChooseToShuffleIntoLibrary(GameData gameData, StackEntry entry,
                                                               int count) {
        resolveHandRevealAndChoose(gameData, entry, count, List.of(), List.of(), null,
                false, false, null, false, false, 0, false, true, true, false, false);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, boolean exileAllCopiesOfChosenNames,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean revealHand) {

        resolveHandRevealAndChoose(gameData, entry, count, excludedTypes, includedTypes, filter,
                discardMode, exileMode, sourcePermanentId, optional, exileAllCopiesOfChosenNames,
                declineFallbackDiscardCount, imprintOnSource, revealHand, false, false, false);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             CardPredicate filter, boolean discardMode, boolean exileMode, UUID sourcePermanentId,
                                             boolean optional, boolean exileAllCopiesOfChosenNames,
                                             int declineFallbackDiscardCount, boolean imprintOnSource,
                                             boolean revealHand, boolean shuffleIntoLibraryMode,
                                             boolean grantPlayPermission, boolean returnAtNextEndStep) {

        boolean effectiveOptional = optional || declineFallbackDiscardCount > 0;
        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String actionVerb = exileMode ? "exile"
                : shuffleIntoLibraryMode ? "shuffle into their library" : "discard";

        if (hand == null || hand.isEmpty()) {
            String logEntry = revealHand
                    ? targetName + " reveals their hand. It is empty."
                    : casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        if (revealHand) {
            GameLog.Builder revealBuilder = GameLog.builder().text(targetName + " reveals their hand: ");
            appendCardList(revealBuilder, hand);
            revealBuilder.text(".");
            gameLogService.append(gameData, revealBuilder.build());
        } else {
            cardRevealService.lookAtHand(gameData, casterId, targetPlayerId);
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
                typeMatches = !excludedTypes.contains(handCard.getType());
            }
            if (typeMatches
                    && (filter == null || predicateEvaluationService.matchesCardPredicate(handCard, filter, sourceCardId))) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices).";
            gameLogService.append(gameData, GameLog.text(noValidEntry));
            log.info("Game {} - {}'s hand has no valid choices for {}", gameData.id, targetName, casterName);
            // No legal choice means the caster doesn't choose one, so the fallback discard applies.
            if (declineFallbackDiscardCount > 0) {
                resolveDiscardCards(gameData, targetPlayerId, declineFallbackDiscardCount);
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
                    + " card to " + actionVerb + ".";
        } else if (shuffleIntoLibraryMode) {
            choicePrompt = (choiceOptional ? "You may choose a card to " : "Choose a card to ")
                    + actionVerb + ".";
        } else {
            choicePrompt = (choiceOptional ? "You may choose a nonland card to " : "Choose a nonland card to ")
                    + actionVerb + ".";
        }
        // sourcePermanentId tracks exile-until-source-leaves effects or an imprint.
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, cardsToChoose, discardMode, exileMode,
                List.of(), sourcePermanentId, choicePrompt, false, effectiveOptional, false,
                null, null, declineFallbackDiscardCount, filter, exileAllCopiesOfChosenNames,
                imprintOnSource, shuffleIntoLibraryMode, false, grantPlayPermission, returnAtNextEndStep));

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to {}",
                gameData.id, casterName, cardsToChoose, targetName, actionVerb);
    
    }

    /**
     * Distended Mindbender: reveal the target's hand, then choose one card matching {@code firstFilter}
     * (if any) and one matching {@code secondFilter} (if any); those are discarded. The second band
     * rides {@link PendingInteraction.RevealedHandChoice#followUpFilter()}.
     */
    public void resolveHandRevealAndChooseTwoFilters(GameData gameData, StackEntry entry,
                                                     CardPredicate firstFilter, CardPredicate secondFilter) {

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(targetName + " reveals their hand. It is empty."));
            log.info("Game {} - {}'s hand is empty for dual-filter hand discard", gameData.id, targetName);
            return;
        }

        GameLog.Builder revealBuilder = GameLog.builder().text(targetName + " reveals their hand: ");
        appendCardList(revealBuilder, hand);
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());

        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        List<Integer> firstIndices = matchingHandIndices(hand, firstFilter, sourceCardId);
        List<Integer> secondIndices = matchingHandIndices(hand, secondFilter, sourceCardId);

        if (firstIndices.isEmpty() && secondIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices)."));
            log.info("Game {} - {}'s hand has no dual-filter matches for {}", gameData.id, targetName, casterName);
            return;
        }

        gameData.discardCausedByOpponent = true;

        String firstPrompt = "Choose a " + CardPredicateUtils.describeFilter(firstFilter) + " to discard.";
        String secondPrompt = "Choose a " + CardPredicateUtils.describeFilter(secondFilter) + " to discard.";

        if (firstIndices.isEmpty()) {
            // Skip the empty first band — only the second band is choosable.
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                    casterId, targetPlayerId, secondIndices, 1, true, false,
                    List.of(), null, secondPrompt, false, false, false, null, null));
            log.info("Game {} - {} choosing second-band card only from {}'s hand",
                    gameData.id, casterName, targetName);
            return;
        }

        CardPredicate followUp = secondIndices.isEmpty() ? null : secondFilter;
        String followUpPrompt = followUp == null ? null : secondPrompt;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, firstIndices, 1, true, false,
                List.of(), null, firstPrompt, false, false, false, followUp, followUpPrompt));

        log.info("Game {} - {} choosing dual-filter cards from {}'s hand (first band{}; follow-up {})",
                gameData.id, casterName, targetName,
                firstIndices.size(), followUp != null);
    }

    private List<Integer> matchingHandIndices(List<Card> hand, CardPredicate filter, UUID sourceCardId) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(hand.get(i), filter, sourceCardId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Vendilion Clique: the caster looks at the target player's hand, then may choose a nonland
     * card. The choice is optional; the chosen card is revealed, put on the bottom of that
     * player's library, and they draw a card ({@code bottomThenDrawMode}). Handled by
     * {@link com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService#handleRevealedHandCardChosen}.
     */
    public void resolveLookAtHandChooseNonlandToBottom(GameData gameData, StackEntry entry) {

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        GameLog.Builder revealBuilder = GameLog.builder().text(casterName + " looks at " + targetName + "'s hand: ");
        appendCardList(revealBuilder, hand);
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!hand.get(i).hasType(CardType.LAND)) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " chooses no card (" + targetName + " has no nonland cards).";
            gameLogService.append(gameData, GameLog.text(noValidEntry));
            log.info("Game {} - {}'s hand has no nonland cards for {}", gameData.id, targetName, casterName);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, 1, false, false, List.of(), null,
                "You may choose a nonland card to put on the bottom of " + targetName + "'s library.",
                true, true));

        log.info("Game {} - {} may choose a nonland card from {}'s hand (bottom + draw)",
                gameData.id, casterName, targetName);
    }

    /**
     * Oildeep Gearhulk: the caster looks at the target player's hand, then may choose a card for
     * that player to discard and draw a card.
     */
    public void resolveLookAtHandChooseCardToDiscardAndDraw(GameData gameData, StackEntry entry) {

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        cardRevealService.lookAtHand(gameData, casterId, targetPlayerId);
        gameData.discardCausedByOpponent = !casterId.equals(targetPlayerId);

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, 1, true, false, List.of(), null,
                "You may choose a card to discard. If you do, that player draws a card.",
                false, true, false, null, null, 0, null, false, false, false, true));

        log.info("Game {} - {} may choose a card from {}'s hand to discard and draw",
                gameData.id, casterName, targetName);
    }

    /**
     * Begins the Blackmail flow: "Target player reveals {@code revealCount} cards from their hand
     * and you choose one of them. That player discards that card." The target picks which cards to
     * reveal; if they hold {@code revealCount} or fewer, their whole hand is revealed and the
     * controller's discard choice begins immediately.
     */
    public void beginRevealCardsChooseDiscard(GameData gameData, StackEntry entry, int revealCount, int discardCount) {
        beginRevealCardsChooseDiscard(gameData, entry, revealCount, discardCount, HandChoiceDestination.DISCARD);
    }

    /**
     * As {@link #beginRevealCardsChooseDiscard(GameData, StackEntry, int, int)}, but the
     * controller's pick goes to {@code destination} — {@code EXILE} exiles it from the target's hand
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
            gameData.discardCausedByOpponent = !controllerId.equals(targetPlayerId);
        }

        if (hand.size() <= revealCount) {
            // Whole hand is revealed — no choice for the target player.
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
        // sibling — carried forward once the reveal picks complete (see the discard-stage begin).
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                targetPlayerId, targetPlayerId, controllerId, true, validIndices, revealCount,
                new ArrayList<>(), discardCount, destination, sourcePermanentId));

        log.info("Game {} - {} choosing {} cards to reveal for reveal-and-discard",
                gameData.id, targetName, revealCount);
    }

    /**
     * Logs the revealed cards and begins the controller's discard choice over exactly that
     * revealed set (the rest of the target's hand stays hidden). The controller discards up to
     * {@code discardCount} of the revealed cards (fewer if the hand held fewer).
     */
    public void beginRevealCardsDiscardStage(GameData gameData, UUID targetPlayerId,
                                             UUID controllerId, List<UUID> revealedCardIds, int discardCount,
                                             HandChoiceDestination destination) {
        beginRevealCardsDiscardStage(gameData, targetPlayerId, controllerId, revealedCardIds, discardCount,
                destination, null);
    }

    /** Begins the controller's pick while preserving a source permanent for source-linked exile. */
    public void beginRevealCardsDiscardStage(GameData gameData, UUID targetPlayerId,
                                             UUID controllerId, List<UUID> revealedCardIds, int discardCount,
                                             HandChoiceDestination destination, UUID sourcePermanentId) {

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> revealedCards = revealedCardIds.stream()
                .map(id -> hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        GameLog.Builder revealBuilder = GameLog.builder().text(targetName + " reveals ");
        appendCardList(revealBuilder, revealedCards);
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < revealedCardIds.size(); i++) {
            validIndices.add(i);
        }

        int toDiscard = Math.min(discardCount, revealedCardIds.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                controllerId, targetPlayerId, controllerId, false, validIndices, toDiscard,
                new ArrayList<>(revealedCardIds), toDiscard, destination, sourcePermanentId));
    }

    /**
     * Begins the next discard pick over the still-revealed cards (used when the controller discards
     * more than one, e.g. Noggin Whack). Unlike {@link #beginRevealCardsDiscardStage} this does not
     * re-log the reveal — the cards were already revealed at the start of the discard stage.
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
            gameData.discardCausedByOpponent = !nextPlayerId.equals(followUp.eachPlayerControllerId());
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
