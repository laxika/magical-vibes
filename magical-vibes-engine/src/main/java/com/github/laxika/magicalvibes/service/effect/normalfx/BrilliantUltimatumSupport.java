package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Completes the Brilliant Ultimatum pile-separation flow once an opponent has assigned the exiled
 * cards to piles and the controller has chosen a pile: the chosen pile's cards are offered to be
 * played/cast for free from exile (lands played subject to the one-land-per-turn limit, spells
 * cast without paying their mana costs). Everything not played remains exiled.
 *
 * @see BrilliantUltimatumEffectHandler
 */
@Slf4j
@Component
public class BrilliantUltimatumSupport {

    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport;
    private final InputCompletionService inputCompletionService;

    // @Lazy mirrors ExileFreeCastSupport: breaks cycles through the interaction registry / input services.
    public BrilliantUltimatumSupport(GameBroadcastService gameBroadcastService,
                                     BattlefieldEntryService battlefieldEntryService,
                                     InteractionHandlerRegistry interactionHandlerRegistry,
                                     @Lazy PlayerInputService playerInputService,
                                     @Lazy ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport,
                                     @Lazy InputCompletionService inputCompletionService) {
        this.gameBroadcastService = gameBroadcastService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.playerInputService = playerInputService;
        this.improvisationCapstoneCastSupport = improvisationCapstoneCastSupport;
        this.inputCompletionService = inputCompletionService;
    }

    /**
     * Step 1: the opponent has assigned cards to Pile 1 (unselected cards form Pile 2). Prompt the
     * controller to choose which pile to play from.
     */
    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1CardIds) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);

        List<UUID> pile1 = new ArrayList<>(pile1CardIds);
        List<UUID> pile2 = new ArrayList<>();
        for (Card card : state.cards()) {
            if (!pile1CardIds.contains(card.getId())) {
                pile2.add(card.getId());
            }
        }

        // Re-queue with the piles filled — step 2 (the pile-choice may prompt) polls it.
        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), state.targetPlayerId(),
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2, true));

        String pile1Desc = describePile(state.cards(), pile1);
        String pile2Desc = describePile(state.cards(), pile2);

        String opponentName = gameData.playerIdToName.get(state.targetPlayerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName
                + " separates cards into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + "."));

        String prompt = "Choose a pile to play lands and cast spells from. Yes = Pile 1 ("
                + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, state.controllerId(), List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Step 2: the controller has chosen a pile. Offer that pile's cards to be played/cast for free
     * from exile; the other pile stays exiled.
     */
    public void completePileSeparationStep2(GameData gameData, boolean accepted) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> chosenPile = accepted ? state.pile1Ids() : state.pile2Ids();
        String chosenName = accepted ? "Pile 1" : "Pile 2";

        String controllerName = gameData.playerIdToName.get(state.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses " + chosenName + "."));

        // Only the cards still in exile are playable (all of them at this point).
        List<UUID> playable = new ArrayList<>();
        for (UUID cardId : chosenPile) {
            if (gameData.findExiledCard(cardId) != null) {
                playable.add(cardId);
            }
        }

        if (playable.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " has no cards to play from " + chosenName + "."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.BrilliantUltimatumPlayChoice(state.controllerId(), playable, playable.size()));
    }

    /**
     * Applies the controller's play choice: lands are put onto the battlefield (subject to the
     * one-land-per-turn limit) and spells are cast without paying their mana costs. Cards left
     * unchosen (or lands that can't be played) remain exiled.
     */
    public void playChosenCards(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        UUID playerId = player.getId();
        List<UUID> spellCardIds = new ArrayList<>();

        if (cardIds != null) {
            for (UUID cardId : cardIds) {
                ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
                if (exiledEntry == null) {
                    continue;
                }
                Card card = exiledEntry.card();
                if (card.hasType(CardType.LAND)) {
                    playLandFromExile(gameData, playerId, card);
                } else {
                    spellCardIds.add(cardId);
                }
            }
        }

        if (!spellCardIds.isEmpty()) {
            // Reuses the shared exile free-cast queue (handles targeting, the stack, and cast triggers).
            improvisationCapstoneCastSupport.castChosenSpellsWithoutPaying(gameData, player, spellCardIds);
            return;
        }

        gameBroadcastService.broadcastGameState(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void playLandFromExile(GameData gameData, UUID playerId, Card card) {
        String playerName = gameData.playerIdToName.get(playerId);
        boolean isControllersTurn = playerId.equals(gameData.activePlayerId);
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        if (!isControllersTurn || landsPlayed >= gameData.getMaxLandsThisTurn(playerId)) {
            String reason = !isControllersTurn ? "not your turn" : "land already played this turn";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName()
                    + " can't be played (" + reason + ") and stays exiled."));
            return;
        }

        gameData.removeFromExile(card.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, new Permanent(card));
        gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.playerPlays(playerName, card, " without paying its mana cost."));
        log.info("Game {} - {} plays land {} from exile (Brilliant Ultimatum)", gameData.id, playerName, card.getName());
        battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);
    }

    private String describePile(List<Card> allCards, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID cardId : cardIds) {
            allCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .ifPresent(c -> names.add(c.getName()));
        }
        return String.join(", ", names);
    }
}
