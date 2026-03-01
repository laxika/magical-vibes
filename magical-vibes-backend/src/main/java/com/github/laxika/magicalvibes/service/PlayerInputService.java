package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.XValueChoiceMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInputService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    /**
     * When mind control is active, redirect messages intended for the controlled player
     * to the controlling player instead, so they can make decisions on their behalf.
     */
    private UUID resolveMessageRecipient(GameData gameData, UUID playerId) {
        if (gameData.mindControlledPlayerId != null
                && gameData.mindControlledPlayerId.equals(playerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return playerId;
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.beginCardChoice(AwaitingInput.CARD_CHOICE, playerId, new HashSet<>(validIndices), null);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetPermanentId) {
        gameData.interaction.beginCardChoice(AwaitingInput.TARGETED_CARD_CHOICE, playerId, new HashSet<>(validIndices), targetPermanentId);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand (targeted)", gameData.id, playerName);
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        gameData.interaction.beginPermanentChoice(playerId, new HashSet<>(validIds), gameData.interaction.permanentChoiceContext());
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChoosePermanentMessage(validIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginAnyTargetChoice(GameData gameData, UUID playerId, List<UUID> validPermanentIds, List<UUID> validPlayerIds, String prompt) {
        Set<UUID> allValidIds = new HashSet<>(validPermanentIds);
        allValidIds.addAll(validPlayerIds);
        gameData.interaction.beginPermanentChoice(playerId, allValidIds, gameData.interaction.permanentChoiceContext());
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChoosePermanentMessage(validPermanentIds, validPlayerIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose any target", gameData.id, playerName);
    }

    void beginGraveyardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        boolean allGraveyards = gameData.interaction.graveyardChoiceCardPool() != null;
        gameData.interaction.beginGraveyardChoice(playerId, new HashSet<>(validIndices),
                gameData.interaction.graveyardChoiceDestination(), gameData.interaction.graveyardChoiceCardPool());
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromGraveyardMessage(validIndices, prompt, allGraveyards));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from graveyard", gameData.id, playerName);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        gameData.interaction.beginMultiPermanentChoice(playerId, new HashSet<>(validIds), maxCount);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseMultiplePermanentsMessage(validIds, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanents", gameData.id, playerName, maxCount);
    }

    void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews, int maxCount, String prompt) {
        gameData.interaction.beginMultiGraveyardChoice(playerId, new HashSet<>(validCardIds), maxCount);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} cards from graveyards", gameData.id, playerName, maxCount);
    }

    void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetPermanentId) {
        gameData.interaction.beginColorChoice(playerId, permanentId, etbTargetPermanentId, gameData.interaction.colorChoiceContext());
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetPermanentId, List<Keyword> options) {
        ColorChoiceContext.KeywordGrantChoice choiceContext = new ColorChoiceContext.KeywordGrantChoice(targetPermanentId, options);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> optionNames = options.stream().map(Keyword::name).toList();
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(optionNames, "Choose a keyword to grant."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a keyword", gameData.id, playerName);
    }

    public void beginCardNameChoice(GameData gameData, UUID playerId, Card card) {
        ColorChoiceContext.CardNameChoice choiceContext = new ColorChoiceContext.CardNameChoice(card, playerId);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> cardNames = collectAllCardNamesInGame(gameData);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(cardNames, "Choose a card name."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card name", gameData.id, playerName);
    }

    public void beginSpellCardNameChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<CardType> excludedTypes) {
        ColorChoiceContext.ExileByNameChoice choiceContext = new ColorChoiceContext.ExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes);
        gameData.interaction.beginColorChoice(choosingPlayerId, null, null, choiceContext);

        List<String> cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card name.";
        sessionManager.sendToPlayer(choosingPlayerId, new ChooseColorMessage(cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (exile from zones)", gameData.id, playerName);
    }

    private List<String> collectCardNamesInGameExcluding(GameData gameData, List<CardType> excludedTypes) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of()).stream()
                    .filter(p -> !hasExcludedType(p.getCard(), excludedTypes))
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
            gameData.playerExiledCards.getOrDefault(pid, List.of()).stream()
                    .filter(c -> !hasExcludedType(c, excludedTypes))
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.stream()
                .filter(se -> !hasExcludedType(se.getCard(), excludedTypes))
                .forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    private boolean hasExcludedType(Card card, List<CardType> excludedTypes) {
        if (excludedTypes.contains(card.getType())) {
            return true;
        }
        for (CardType excluded : excludedTypes) {
            if (card.getAdditionalTypes().contains(excluded)) {
                return true;
            }
        }
        return false;
    }

    private List<String> collectAllCardNamesInGame(GameData gameData) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of())
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerExiledCards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    public void beginMultiZoneExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards, UUID targetPlayerId, String cardName) {
        List<UUID> validCardIds = matchingCards.stream().map(Card::getId).toList();
        List<CardView> cardViews = matchingCards.stream().map(cardViewFactory::create).toList();
        int maxCount = matchingCards.size();

        gameData.interaction.beginMultiZoneExileChoice(choosingPlayerId, new HashSet<>(validCardIds), maxCount, targetPlayerId, choosingPlayerId, cardName);
        sessionManager.sendToPlayer(choosingPlayerId, new ChooseMultipleCardsFromGraveyardsMessage(
                validCardIds, cardViews, maxCount,
                "Choose any number of cards named \"" + cardName + "\" to exile."));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose cards to exile (up to {})", gameData.id, playerName, maxCount);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID sourcePermanentId) {
        gameData.interaction.beginCardChoice(AwaitingInput.IMPRINT_FROM_HAND_CHOICE, playerId, new HashSet<>(validIndices), sourcePermanentId);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose an artifact from hand to imprint", gameData.id, playerName);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        beginDiscardChoice(gameData, playerId, validIndices, "Choose a card to discard.");
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.beginCardChoice(AwaitingInput.DISCARD_CHOICE, playerId, new HashSet<>(validIndices), null);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card to discard", gameData.id, playerName);
    }

    public void beginRevealedHandChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.beginRevealedHandChoiceFromCurrentState(
                choosingPlayerId,
                targetPlayerId,
                new HashSet<>(validIndices)
        );

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, choosingPlayerId), new ChooseFromRevealedHandMessage(cardViews, validIndices, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card from revealed hand", gameData.id, playerName);
    }

    public void beginXValueChoice(GameData gameData, UUID playerId, int maxValue, String prompt, String cardName) {
        gameData.interaction.beginXValueChoice(playerId, maxValue, prompt, cardName);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new XValueChoiceMessage(prompt, maxValue, cardName));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose X value (max {})", gameData.id, playerName, maxValue);
    }

    public void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        gameData.interaction.beginMayAbilityChoice(next.controllerId(), next.description());
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, next.controllerId()), new MayAbilityMessage(next.description()));

        String playerName = gameData.playerIdToName.get(next.controllerId());
        log.info("Game {} - Awaiting {} to decide on may ability: {}", gameData.id, playerName, next.description());
    }
}


