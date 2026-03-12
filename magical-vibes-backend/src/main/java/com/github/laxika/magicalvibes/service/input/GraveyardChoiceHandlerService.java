package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final LifeResolutionService lifeResolutionService;

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)) {
            throw new IllegalStateException("Not awaiting graveyard choice");
        }
        InteractionContext.GraveyardChoice graveyardChoice = gameData.interaction.graveyardChoiceContext();
        if (graveyardChoice == null || !player.getId().equals(graveyardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = graveyardChoice.validIndices();
        List<Card> cardPool = graveyardChoice.cardPool();

        gameData.interaction.clearAwaitingInput();
        GraveyardChoiceDestination destination = graveyardChoice.destination();
        boolean gainLifeEqualToManaValue = gameData.interaction.graveyardChoice().gainLifeEqualToManaValue();
        UUID attachToSourcePermanentId = gameData.interaction.graveyardChoice().attachToSourcePermanentId();
        CardColor grantColor = gameData.interaction.graveyardChoice().grantColor();
        CardSubtype grantSubtype = gameData.interaction.graveyardChoice().grantSubtype();
        gameData.interaction.clearGraveyardChoice();

        if (cardIndex == -1) {
            // Player declined
            String logEntry = player.getUsername() + " chooses not to return a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to return a card from graveyard", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card card;
            if (cardPool != null) {
                // Cross-graveyard choice: card pool contains cards from any graveyard
                card = cardPool.get(cardIndex);
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            } else {
                // Standard choice: indices into the player's own graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                card = graveyard.remove(cardIndex);
            }

            switch (destination) {
                case HAND -> {
                    gameData.playerHands.get(playerId).add(card);

                    String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

                    if (gainLifeEqualToManaValue) {
                        int manaValue = card.getManaValue();
                        if (manaValue > 0) {
                            lifeResolutionService.applyGainLife(gameData, playerId, manaValue);
                        }
                    }
                }
                case BATTLEFIELD -> {
                    Permanent perm = new Permanent(card);
                    if (grantColor != null) {
                        perm.getGrantedColors().add(grantColor);
                    }
                    if (grantSubtype != null && !perm.getGrantedSubtypes().contains(grantSubtype)) {
                        perm.getGrantedSubtypes().add(grantSubtype);
                    }
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm);

                    String logEntry = player.getUsername() + " puts " + card.getName() + " from a graveyard onto the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} puts {} from graveyard onto battlefield", gameData.id, player.getUsername(), card.getName());

                    if (attachToSourcePermanentId != null) {
                        Permanent sourcePerm = gameQueryService.findPermanentById(gameData, attachToSourcePermanentId);
                        if (sourcePerm != null) {
                            gameData.interaction.setPendingEquipmentAttach(perm.getId(), sourcePerm.getId());
                            gameData.pendingMayAbilities.add(new PendingMayAbility(
                                    sourcePerm.getCard(), playerId, List.of(),
                                    sourcePerm.getCard().getName() + " — Attach " + card.getName()
                                            + " to " + sourcePerm.getCard().getName() + "?"
                            ));
                            playerInputService.processNextMayAbility(gameData);
                        }
                    }

                    if (card.getType() == CardType.CREATURE) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, playerId);
                    }
                }
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MULTI_GRAVEYARD_CHOICE)) {
            throw new IllegalStateException("Not awaiting multi-graveyard choice");
        }
        InteractionContext.MultiGraveyardChoice multiGraveyardChoice = gameData.interaction.multiGraveyardChoiceContext();
        if (multiGraveyardChoice == null || !player.getId().equals(multiGraveyardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = multiGraveyardChoice.validCardIds();
        int maxCount = multiGraveyardChoice.maxCount();

        if (cardIds == null) {
            cardIds = List.of();
        }

        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
        }

        // Spell targeting (e.g. Midnight Ritual) requires exactly X targets — "X target" is not "up to X target"
        // Exception: "any number of target" spells (e.g. Frantic Salvage) allow 0 to max
        StackEntryType pendingEntryTypeCheck = gameData.graveyardTargetOperation.entryType;
        int pendingXValueCheck = gameData.graveyardTargetOperation.xValue;
        boolean isAnyNumber = gameData.graveyardTargetOperation.anyNumber;
        if (pendingEntryTypeCheck != null && !isAnyNumber && cardIds.size() != pendingXValueCheck) {
            throw new IllegalStateException("Must choose exactly " + pendingXValueCheck + " targets, but chose " + cardIds.size());
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        // Retrieve the pending info
        Card pendingCard = gameData.graveyardTargetOperation.card;
        UUID controllerId = gameData.graveyardTargetOperation.controllerId;
        List<CardEffect> pendingEffects = gameData.graveyardTargetOperation.effects;
        StackEntryType pendingEntryType = gameData.graveyardTargetOperation.entryType;
        int pendingXValue = gameData.graveyardTargetOperation.xValue;

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMultiGraveyardChoice();
        gameData.graveyardTargetOperation.card = null;
        gameData.graveyardTargetOperation.controllerId = null;
        gameData.graveyardTargetOperation.effects = null;
        gameData.graveyardTargetOperation.entryType = null;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = false;

        List<String> targetNames = new ArrayList<>();
        for (UUID cardId : cardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                targetNames.add(card.getName());
            }
        }

        if (pendingEntryType != null) {
            // Spell casting (e.g. Midnight Ritual) — put spell on stack with targets
            gameData.stack.add(new StackEntry(
                    pendingEntryType, pendingCard, controllerId, pendingCard.getName(),
                    new ArrayList<>(pendingEffects), pendingXValue, null,
                    null, Map.of(), null, new ArrayList<>(cardIds), List.of()
            ));

            gameData.spellsCastThisTurn.merge(controllerId, 1, Integer::sum);
            gameData.priorityPassedBy.clear();

            String castLog = gameData.playerIdToName.get(controllerId) + " casts " + pendingCard.getName()
                    + " targeting " + String.join(", ", targetNames) + ".";
            gameBroadcastService.logAndBroadcast(gameData, castLog);
            log.info("Game {} - {} casts {} with {} graveyard targets", gameData.id, pendingCard.getName(),
                    pendingCard.getName(), cardIds.size());

            triggerCollectionService.checkSpellCastTriggers(gameData, pendingCard, controllerId);
            gameBroadcastService.broadcastGameState(gameData);
        } else {
            // ETB ability — put triggered ability on stack with targets
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    pendingCard,
                    controllerId,
                    pendingCard.getName() + "'s ETB ability",
                    new ArrayList<>(pendingEffects),
                    new ArrayList<>(cardIds)
            ));

            if (cardIds.isEmpty()) {
                String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting no cards.";
                gameBroadcastService.logAndBroadcast(gameData, etbLog);
            } else {
                String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting " + String.join(", ", targetNames) + ".";
                gameBroadcastService.logAndBroadcast(gameData, etbLog);
            }
            log.info("Game {} - {} ETB ability pushed onto stack with {} graveyard targets", gameData.id, pendingCard.getName(), cardIds.size());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}


