package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.networking.SessionManager;
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
        beginCardChoice(gameData, playerId, validIndices, prompt, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, boolean enterTapped) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped));
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking));
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
        beginMultiPermanentChoice(gameData, playerId, validIds, maxCount, null, prompt);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount,
                                          MultiPermanentChoiceContext context, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiPermanentChoice(
                playerId, new ArrayList<>(validIds), maxCount, context, prompt));
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

    public void beginDiscardChosenColorChoice(GameData gameData, UUID controllerId, UUID targetPlayerId) {
        ChoiceContext.DiscardChosenColorChoice ctx = new ChoiceContext.DiscardChosenColorChoice(controllerId, targetPlayerId);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (discard all cards of that color)", gameData.id, playerName);
    }

    public void beginExileTopCardsChosenColorTokensChoice(GameData gameData, UUID controllerId, UUID targetPlayerId,
            int count, com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate, String sourceSetCode) {
        ChoiceContext.ExileTopCardsChosenColorTokensChoice ctx =
                new ChoiceContext.ExileTopCardsChosenColorTokensChoice(controllerId, targetPlayerId, count, tokenTemplate, sourceSetCode);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (Oona-style exile/token)", gameData.id, playerName);
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

    public void beginColorSetChoice(GameData gameData, UUID controllerId, UUID targetId, String sourceCardName) {
        ChoiceContext.ColorSetChoice ctx = new ChoiceContext.ColorSetChoice(targetId, controllerId, sourceCardName);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (target becomes chosen color)", gameData.id, playerName);
    }

    /**
     * Prismwake Merrow: prompt {@code playerId} to pick a color for {@code targetId}. Colors are
     * picked one at a time — already-chosen colors are dropped from the options, and "DONE" is
     * offered once at least one color has been chosen. The choice handler accumulates the picks and
     * re-invokes this until the player is done (see {@code ChoiceHandlerService}).
     */
    public void beginBecomeChosenColorsChoice(GameData gameData, UUID playerId, UUID targetId,
                                              String sourceCardName, List<CardColor> chosen) {
        ChoiceContext.BecomeChosenColorsChoice ctx =
                new ChoiceContext.BecomeChosenColorsChoice(targetId, sourceCardName, new ArrayList<>(chosen));

        List<String> options = new ArrayList<>();
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)) {
            if (!chosen.contains(color)) {
                options.add(color.name());
            }
        }
        if (!chosen.isEmpty()) {
            options.add("DONE");
        }
        String prompt = chosen.isEmpty() ? "Choose a color." : "Choose another color, or DONE.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
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

    public void beginSpellCreatureTypeChoice(GameData gameData, UUID playerId) {
        ChoiceContext.SpellCreatureTypeChoice choiceContext = new ChoiceContext.SpellCreatureTypeChoice(playerId);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    public void beginManaValueParityChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.ManaValueParityChoice choiceContext = new ChoiceContext.ManaValueParityChoice(permanentId);

        List<String> options = List.of("ODD", "EVEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose odd or even."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose odd or even", gameData.id, playerName);
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
        beginAddBasicLandTypeChoice(gameData, playerId, targetLandId, duration, false);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, EffectDuration duration, boolean replacing) {
        ChoiceContext.AddBasicLandTypeChoice choiceContext = new ChoiceContext.AddBasicLandTypeChoice(targetLandId, duration, replacing);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type to add", gameData.id, playerName);
    }

    public void beginOwnLandsBecomeBasicTypeChoice(GameData gameData, UUID playerId) {
        ChoiceContext.OwnLandsBecomeBasicTypeChoice choiceContext = new ChoiceContext.OwnLandsBecomeBasicTypeChoice(playerId);

        List<String> basicLandTypes = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type for their lands", gameData.id, playerName);
    }

    public void beginStorageMatrixUntapChoice(GameData gameData, UUID playerId) {
        ChoiceContext.StorageMatrixUntapChoice choiceContext = new ChoiceContext.StorageMatrixUntapChoice(playerId);

        List<String> options = List.of("ARTIFACT", "CREATURE", "LAND");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose artifact, creature, or land to untap."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type to untap (Storage Matrix)", gameData.id, playerName);
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
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, null, remainingCount);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = allHandIndices(hand);

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileFromHandChoice(
                playerId, validIndices, sourcePermanentId, playPermissionControllerId, remainingCount,
                "Choose a card to exile."));
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount) {
        beginDiscardChoice(gameData, playerId, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount, DiscardFollowUp followUp) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount, followUp);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount, DiscardFollowUp followUp) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardChoice(
                playerId, new ArrayList<>(validIndices), remainingCount, followUp, prompt));
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


