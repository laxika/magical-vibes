package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileNonlandCardFromTargetHandOrGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice> {

    private final ExileService exileService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice> handledType() {
        return PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null || chosenCardIds.size() != 1
                || !interaction.validCardIds().contains(chosenCardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one valid nonland card");
        }

        UUID chosenCardId = chosenCardIds.getFirst();
        UUID targetPlayerId = interaction.targetPlayerId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of());
        Card chosenCard = findCard(hand, chosenCardId);
        boolean fromHand = chosenCard != null;
        if (chosenCard == null) {
            chosenCard = findCard(graveyard, chosenCardId);
        }
        if (chosenCard == null || chosenCard.hasType(com.github.laxika.magicalvibes.model.CardType.LAND)) {
            throw new IllegalStateException("Chosen card is no longer a valid nonland card");
        }

        if (fromHand) {
            hand.remove(chosenCard);
        } else {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, chosenCardId);
        }
        exileService.exileCard(gameData, targetPlayerId, chosenCard);
        gameData.exilePlayPermissions.put(chosenCardId, interaction.playerId());
        gameData.exilePlayAnyManaTypeWhileExiled.add(chosenCardId);

        String chooserName = gameData.playerIdToName.get(interaction.playerId());
        String zone = fromHand ? "hand" : "graveyard";
        gameLogService.append(gameData,
                GameLog.textCardText(chooserName + " exiles ", chosenCard, " from an opponent's " + zone + "."));

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
