package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpponentChoosesCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (OpponentChoosesCardFromGraveyardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        UUID chosenCardId = gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId;
        if (chosenCardId != null) {
            gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            returnCardToHand(gameData, entry, chosenCardId, choiceEffect);
            return;
        }

        if (choiceEffect.defendingPlayerChooses()) {
            UUID defendingPlayerId = defendingPlayerId(gameData, entry);
            if (defendingPlayerId == null) {
                gameData.rerunCurrentEffectAfterInteraction = false;
                return;
            }
            beginCardChoice(gameData, entry, choiceEffect, defendingPlayerId);
            return;
        }

        UUID chosenOpponentId = gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenOpponentId;
        if (chosenOpponentId != null) {
            gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenOpponentId = null;
            beginCardChoice(gameData, entry, choiceEffect, chosenOpponentId);
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }
        if (opponents.size() == 1) {
            beginCardChoice(gameData, entry, choiceEffect, opponents.getFirst());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.OpponentChoosesCardFromGraveyardToHand());
        playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                sourceName + " — choose an opponent.");
    }

    /** Completes the controller's opponent choice and resumes the parked ability. */
    public void completeOpponentChoice(GameData gameData, UUID opponentId) {
        gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenOpponentId = opponentId;
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void beginCardChoice(GameData gameData, StackEntry entry,
                                 OpponentChoosesCardFromGraveyardToHandEffect choiceEffect,
                                 UUID opponentId) {
        CardPredicate filter = choiceEffect.filter();
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return;
        }

        List<Card> matchingCards = graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, entry.getCard().getId()))
                .toList();
        if (matchingCards.isEmpty()) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }
        if (matchingCards.size() == 1) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            returnCardToHand(gameData, entry, matchingCards.getFirst().getId(), choiceEffect);
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        opponentId,
                        IntStream.range(0, matchingCards.size()).boxed().toList(),
                        GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName() + " — choose a nonland card in your opponent's graveyard.")
                .cardPool(matchingCards)
                .mandatory(true)
                .build());
    }

    private void returnCardToHand(GameData gameData, StackEntry entry, UUID cardId,
                                  OpponentChoosesCardFromGraveyardToHandEffect choiceEffect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }
        Card card = graveyard.stream()
                .filter(candidate -> candidate.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (card == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        gameData.addCardToHand(controllerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                sourceName + " returns ", card, " from the graveyard to its owner's hand."));

        if (choiceEffect.boostSourceByChosenCardManaValue() && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                int manaValue = card.getManaValue();
                source.setPowerModifier(source.getPowerModifier() + manaValue);
                gameLogService.append(gameData, GameLog.builder()
                        .card(source.getCard())
                        .text(" gets +" + manaValue + "/+0 until end of turn.")
                        .build());
            }
        }
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        return gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
