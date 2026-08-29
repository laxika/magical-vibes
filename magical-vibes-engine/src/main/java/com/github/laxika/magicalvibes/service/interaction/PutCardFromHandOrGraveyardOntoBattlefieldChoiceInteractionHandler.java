package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardOntoBattlefieldChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardFromHandOrGraveyardOntoBattlefieldChoice> {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PutCardFromHandOrGraveyardOntoBattlefieldChoice> handledType() {
        return PendingInteraction.PutCardFromHandOrGraveyardOntoBattlefieldChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardFromHandOrGraveyardOntoBattlefieldChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null || chosenCardIds.size() != 1
                || !interaction.validCardIds().contains(chosenCardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one valid card");
        }

        UUID chosenCardId = chosenCardIds.getFirst();
        List<Card> hand = gameData.playerHands.getOrDefault(interaction.playerId(), List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(interaction.playerId(), List.of());
        Card chosenCard = findCard(hand, chosenCardId);
        boolean fromHand = chosenCard != null;
        if (chosenCard == null) {
            chosenCard = findCard(graveyard, chosenCardId);
        }
        if (chosenCard == null || !predicateEvaluationService.matchesCardPredicate(
                chosenCard, interaction.predicate(), interaction.sourceCardId(), gameData, interaction.playerId())) {
            throw new IllegalStateException("Chosen card is no longer valid");
        }

        if (fromHand) {
            hand.remove(chosenCard);
            Permanent permanent = new Permanent(chosenCard);
            if (interaction.enterWithCounter() != null
                    && interaction.enterWithCounter() != CounterType.FINALITY
                    && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setCounterCount(interaction.enterWithCounter(), 1);
            }
            if (interaction.enterWithCounter() == CounterType.FINALITY) {
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, interaction.playerId(), permanent, 0, false, 1);
            } else {
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, interaction.playerId(), permanent);
            }
            if (gameData.playerBattlefields.getOrDefault(interaction.playerId(), List.of()).contains(permanent)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, interaction.playerId(), chosenCard, null, false);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(interaction.playerId()) + " puts ", chosenCard,
                    " onto the battlefield from their hand."));
        } else {
            permanentRemovalService.removeCardFromGraveyardById(gameData, chosenCardId);
            graveyardReturnSupport.putCardOntoBattlefield(
                    gameData, interaction.playerId(), chosenCard, null, null, false, false,
                    interaction.enterWithCounter());
        }

        gameData.interaction.clearAwaitingInput();
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private static Card findCard(List<Card> cards, UUID cardId) {
        return cards.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
