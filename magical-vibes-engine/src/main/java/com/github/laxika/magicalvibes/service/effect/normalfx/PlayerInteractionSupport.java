package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
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
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void applyOpponentMayPlayCreature(GameData gameData, UUID controllerId) {

        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        resolvePlayerMayPlayCreature(gameData, opponentId);
    
    }
    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect) {
        applyPutCardToBattlefield(gameData, playerId, effect, 0);
    }

    public void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect, int xValue) {

        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card handCard = hand.get(i);
                if (!predicateEvaluationService.matchesCardPredicate(handCard, effect.predicate(), handCard.getId())) {
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no {} cards in hand for hand-to-battlefield effect", gameData.id, playerName, effect.label());
            return;
        }

        String tappedSuffix = effect.enterTapped() ? " tapped" : "";
        String prompt = "Choose a " + effect.label() + " card from your hand to put onto the battlefield" + tappedSuffix + ".";
        playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt, effect.enterTapped());

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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for creature-choice effect", gameData.id, playerName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, creatureIndices, prompt);
    
    }
    public void applyDrawCards(GameData gameData, UUID playerId, int amount) {

        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
    
    }
    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        resolveDiscardCards(gameData, playerId, amount, DiscardFollowUp.NONE);
    }

    public void resolveDiscardCards(GameData gameData, UUID playerId, int amount, DiscardFollowUp followUp) {

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        playerInputService.beginDiscardChoice(gameData, playerId, amount, followUp);

    }
    public void resolveRandomDiscardCards(GameData gameData, UUID playerId, String sourceName, int amount) {

        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        for (int i = 0; i < amount; i++) {
            List<Card> currentHand = gameData.playerHands.get(playerId);
            if (currentHand.isEmpty()) break;
            int randomIndex = ThreadLocalRandom.current().nextInt(currentHand.size());
            Card discarded = currentHand.remove(randomIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, discarded);
            String logEntry = playerName + " discards " + discarded.getName() + " at random.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    
    }
    public void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             boolean discardMode, boolean exileMode, UUID sourcePermanentId) {

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String actionVerb = exileMode ? "exile" : "discard";

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = targetName + " reveals their hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        // Build valid indices based on included or excluded types
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card handCard = hand.get(i);
            if (!includedTypes.isEmpty()) {
                // Included mode: card must match at least one included type (primary or additional)
                boolean matches = includedTypes.contains(handCard.getType())
                        || handCard.getAdditionalTypes().stream().anyMatch(includedTypes::contains);
                if (matches) {
                    validIndices.add(i);
                }
            } else if (!excludedTypes.contains(handCard.getType())) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices).";
            gameBroadcastService.logAndBroadcast(gameData, noValidEntry);
            log.info("Game {} - {}'s hand has no valid choices for {}", gameData.id, targetName, casterName);
            return;
        }

        int cardsToChoose = Math.min(count, validIndices.size());

        String choicePrompt;
        if (!includedTypes.isEmpty()) {
            String typeNames = includedTypes.stream()
                    .map(CardType::getDisplayName)
                    .reduce((a, b) -> a + " or " + b)
                    .orElse("card");
            choicePrompt = "Choose a " + typeNames.toLowerCase() + " card to " + actionVerb + ".";
        } else {
            choicePrompt = "Choose a nonland card to " + actionVerb + ".";
        }
        // sourcePermanentId tracks exile-until-source-leaves effects (e.g. Kitesail Freebooter)
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, cardsToChoose, discardMode, exileMode,
                List.of(), sourcePermanentId, choicePrompt));

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to {}",
                gameData.id, casterName, cardsToChoose, targetName, actionVerb);
    
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
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    
    }
    public void startNextEachPlayerDiscard(GameData gameData, DiscardFollowUp followUp) {

        int amount = followUp.eachPlayerAmount();
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerDiscards());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            gameData.discardCausedByOpponent = !nextPlayerId.equals(followUp.eachPlayerControllerId());
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            if (hand == null || hand.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(nextPlayerId) + " has no cards to discard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }
            playerInputService.beginDiscardChoice(gameData, nextPlayerId, amount,
                    followUp.withRemainingEachPlayerDiscards(remaining));
            return;
        }

    }
}
