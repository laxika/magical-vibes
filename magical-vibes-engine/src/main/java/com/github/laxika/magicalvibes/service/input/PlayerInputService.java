package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInputService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt));
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandCardChoice(
                playerId, new ArrayList<>(validIndices), targetId, prompt));
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(validIds), List.of(),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginAnyTargetChoice(GameData gameData, UUID playerId, List<UUID> validPermanentIds, List<UUID> validPlayerIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(validPermanentIds), new ArrayList<>(validPlayerIds),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose any target", gameData.id, playerName);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiPermanentChoice(
                playerId, new ArrayList<>(validIds), maxCount, prompt));
    }

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<Card> cards, int maxCount, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt));
    }

    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId) {
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, permanentId, etbTargetId, null, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginProtectionColorChoice(GameData gameData, UUID playerId, UUID targetId, boolean includeArtifacts) {
        ChoiceContext.ProtectionColorChoice ctx = new ChoiceContext.ProtectionColorChoice(targetId, includeArtifacts);

        List<String> options = new java.util.ArrayList<>(List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"));
        if (includeArtifacts) {
            options.addFirst("ARTIFACT");
        }
        String prompt = includeArtifacts ? "Choose a color or artifacts." : "Choose a color.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose protection", gameData.id, playerName);
    }

    public void beginMassProtectionColorChoice(GameData gameData, UUID controllerId) {
        ChoiceContext.MassProtectionColorChoice ctx = new ChoiceContext.MassProtectionColorChoice(controllerId);

        List<String> options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, options, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose protection (you and your permanents)", gameData.id, playerName);
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetId, List<Keyword> options) {
        ChoiceContext.KeywordGrantChoice choiceContext = new ChoiceContext.KeywordGrantChoice(targetId, options);

        List<String> optionNames = options.stream().map(Keyword::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, optionNames, "Choose a keyword to grant."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a keyword", gameData.id, playerName);
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.SubtypeChoice choiceContext = new ChoiceContext.SubtypeChoice(permanentId);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    public void beginPermanentTypeChoice(GameData gameData, UUID playerId, GraveyardChoiceDestination destination, String entryDescription) {
        ChoiceContext.PermanentTypeChoice choiceContext = new ChoiceContext.PermanentTypeChoice(playerId, destination, entryDescription);

        List<String> permanentTypes = List.of("ARTIFACT", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, permanentTypes, "Choose a permanent type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type", gameData.id, playerName);
    }

    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.BasicLandTypeChoice choiceContext = new ChoiceContext.BasicLandTypeChoice(permanentId);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type", gameData.id, playerName);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, EffectDuration duration) {
        ChoiceContext.AddBasicLandTypeChoice choiceContext = new ChoiceContext.AddBasicLandTypeChoice(targetLandId, duration);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type to add", gameData.id, playerName);
    }

    private static List<Integer> allHandIndices(List<Card> hand) {
        return IntStream.range(0, hand.size()).boxed().toList();
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND,
            CardSubtype.PLAINS, CardSubtype.SWAMP, CardSubtype.AURA,
            CardSubtype.EQUIPMENT, CardSubtype.LOCUS
    );

    public void beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes) {
        ChoiceContext.CardNameChoice choiceContext = new ChoiceContext.CardNameChoice(card, playerId, excludedTypes);

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
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card name", gameData.id, playerName);
    }

    public void beginSpellCardNameChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<CardType> excludedTypes) {
        ChoiceContext.ExileByNameChoice choiceContext = new ChoiceContext.ExileByNameChoice(targetPlayerId, choosingPlayerId, excludedTypes);

        List<String> cardNames = collectCardNamesInGameExcluding(gameData, excludedTypes);
        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (exile from zones)", gameData.id, playerName);
    }

    public void beginSphinxAmbassadorCardNameChoice(GameData gameData, UUID namingPlayerId, UUID controllerId) {
        ChoiceContext.SphinxAmbassadorNameChoice choiceContext = new ChoiceContext.SphinxAmbassadorNameChoice(namingPlayerId, controllerId);

        List<String> cardNames = collectAllCardNamesInGame(gameData);
        String prompt = "Choose a card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                namingPlayerId, null, null, choiceContext, cardNames, prompt));

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
            gameData.getPlayerExiledCards(pid).stream()
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
        return collectCardNamesInGameExcluding(gameData, List.of());
    }

    public void beginMultiZoneExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards, UUID targetPlayerId, String cardName) {
        List<UUID> validCardIds = matchingCards.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiZoneExileChoice(
                choosingPlayerId, validCardIds, matchingCards.size(), targetPlayerId, choosingPlayerId, cardName));
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID sourcePermanentId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprintFromHandChoice(
                playerId, new ArrayList<>(validIndices), sourcePermanentId, prompt));
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId, int remainingCount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = allHandIndices(hand);

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileFromHandChoice(
                playerId, validIndices, sourcePermanentId, remainingCount, "Choose a card to exile."));
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardChoice(
                playerId, new ArrayList<>(validIndices), remainingCount, prompt));
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
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MayAbilityChoice(
                next.controllerId(), next.description(), next.manaCost()));
    }
}


