package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
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
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.XValueChoiceMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
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
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt, true));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetPermanentId) {
        gameData.interaction.beginCardChoice(AwaitingInput.TARGETED_CARD_CHOICE, playerId, new HashSet<>(validIndices), targetPermanentId);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, prompt, true));

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

    public void beginGraveyardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        boolean allGraveyards = gameData.interaction.graveyardChoice().cardPool() != null;
        gameData.interaction.beginGraveyardChoice(playerId, new HashSet<>(validIndices),
                gameData.interaction.graveyardChoice().destination(), gameData.interaction.graveyardChoice().cardPool());
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

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews, int maxCount, String prompt) {
        gameData.interaction.beginMultiGraveyardChoice(playerId, new HashSet<>(validCardIds), maxCount);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} cards from graveyards", gameData.id, playerName, maxCount);
    }

    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetPermanentId) {
        gameData.interaction.beginColorChoice(playerId, permanentId, etbTargetPermanentId, gameData.interaction.colorChoiceContext());
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginProtectionColorChoice(GameData gameData, UUID playerId, UUID targetPermanentId, boolean includeArtifacts) {
        ChoiceContext.ProtectionColorChoice ctx = new ChoiceContext.ProtectionColorChoice(targetPermanentId, includeArtifacts);
        gameData.interaction.beginColorChoice(playerId, null, null, ctx);

        List<String> options = new java.util.ArrayList<>(List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"));
        if (includeArtifacts) {
            options.addFirst("ARTIFACT");
        }
        String prompt = includeArtifacts ? "Choose a color or artifacts." : "Choose a color.";
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose protection", gameData.id, playerName);
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetPermanentId, List<Keyword> options) {
        ChoiceContext.KeywordGrantChoice choiceContext = new ChoiceContext.KeywordGrantChoice(targetPermanentId, options);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> optionNames = options.stream().map(Keyword::name).toList();
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(optionNames, "Choose a keyword to grant."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a keyword", gameData.id, playerName);
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.SubtypeChoice choiceContext = new ChoiceContext.SubtypeChoice(permanentId);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .map(CardSubtype::name)
                .toList();
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    public void beginPermanentTypeChoice(GameData gameData, UUID playerId, GraveyardChoiceDestination destination, String entryDescription) {
        ChoiceContext.PermanentTypeChoice choiceContext = new ChoiceContext.PermanentTypeChoice(playerId, destination, entryDescription);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> permanentTypes = List.of("ARTIFACT", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(permanentTypes, "Choose a permanent type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type", gameData.id, playerName);
    }

    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.BasicLandTypeChoice choiceContext = new ChoiceContext.BasicLandTypeChoice(permanentId);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type", gameData.id, playerName);
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND,
            CardSubtype.PLAINS, CardSubtype.SWAMP, CardSubtype.AURA,
            CardSubtype.EQUIPMENT, CardSubtype.LOCUS
    );

    public void beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes) {
        ChoiceContext.CardNameChoice choiceContext = new ChoiceContext.CardNameChoice(card, playerId, excludedTypes);
        gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);

        List<String> cardNames;
        String prompt;
        if (excludedTypes.isEmpty()) {
            cardNames = collectAllCardNamesInGame(gameData);
            prompt = "Choose a card name.";
        } else {
            cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
            String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
            prompt = "Choose a non" + excludedLabel + " card name.";
        }
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseColorMessage(cardNames, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card name", gameData.id, playerName);
    }

    public void beginSpellCardNameChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<CardType> excludedTypes) {
        ChoiceContext.ExileByNameChoice choiceContext = new ChoiceContext.ExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes);
        gameData.interaction.beginColorChoice(choosingPlayerId, null, null, choiceContext);

        List<String> cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card name.";
        sessionManager.sendToPlayer(choosingPlayerId, new ChooseColorMessage(cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (exile from zones)", gameData.id, playerName);
    }

    public void beginSphinxAmbassadorCardNameChoice(GameData gameData, UUID namingPlayerId, UUID controllerId) {
        ChoiceContext.SphinxAmbassadorNameChoice choiceContext = new ChoiceContext.SphinxAmbassadorNameChoice(namingPlayerId, controllerId);
        gameData.interaction.beginColorChoice(namingPlayerId, null, null, choiceContext);

        List<String> cardNames = collectAllCardNamesInGame(gameData);
        String prompt = "Choose a card name.";
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, namingPlayerId), new ChooseColorMessage(cardNames, prompt));

        String playerName = gameData.playerIdToName.get(namingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (Sphinx Ambassador)", gameData.id, playerName);
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

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.interaction.beginCardChoice(AwaitingInput.EXILE_FROM_HAND_CHOICE, playerId, new HashSet<>(validIndices), sourcePermanentId);
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId), new ChooseCardFromHandMessage(validIndices, "Choose a card to exile."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card to exile from hand", gameData.id, playerName);
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

    public void sendKnowledgePoolCastChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews) {
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId),
                new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, 1,
                        "Knowledge Pool — you may cast a nonland card without paying its mana cost."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from Knowledge Pool", gameData.id, playerName);
    }

    public void sendMirrorOfFateChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews, int maxCount) {
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId),
                new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, maxCount,
                        "Choose up to seven face-up exiled cards you own to put on top of your library."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose exiled cards for Mirror of Fate (up to {})", gameData.id, playerName, maxCount);
    }

    public void beginLibraryReorderFromExile(GameData gameData, UUID playerId, List<Card> cards) {
        gameData.interaction.beginLibraryReorder(playerId, cards, false);
        List<CardView> cardViews = cards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, playerId),
                new ReorderLibraryCardsMessage(cardViews,
                        "Put these cards on top of your library in any order (top to bottom)."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to order {} cards on top of library", gameData.id, playerName, cards.size());
    }

    public void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (gameData.status == GameStatus.FINISHED) {
            gameData.pendingMayAbilities.clear();
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        gameData.interaction.beginMayAbilityChoice(next.controllerId(), next.description());

        boolean canPay = true;
        if (next.manaCost() != null) {
            ManaCost cost = new ManaCost(next.manaCost());
            ManaPool pool = gameData.playerManaPools.get(next.controllerId());
            canPay = cost.hasX() ? cost.calculateMaxX(pool) > 0 : cost.canPay(pool);
        }

        sessionManager.sendToPlayer(resolveMessageRecipient(gameData, next.controllerId()), new MayAbilityMessage(next.description(), canPay, next.manaCost()));

        String playerName = gameData.playerIdToName.get(next.controllerId());
        log.info("Game {} - Awaiting {} to decide on may ability: {}", gameData.id, playerName, next.description());
    }
}


