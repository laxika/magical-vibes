package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprovisationCapstoneCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ImprovisationCapstoneCastChoice> {

    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<PendingInteraction.ImprovisationCapstoneCastChoice> handledType() {
        return PendingInteraction.ImprovisationCapstoneCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ImprovisationCapstoneCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds.size() > interaction.maxCount()
                || new HashSet<>(cardIds).size() != cardIds.size()
                || !interaction.validCardIds().containsAll(cardIds)
                || cardIds.stream().anyMatch(id -> gameData.findExiledCard(id) == null)) {
            throw new IllegalStateException("Choose distinct cards from the offered exiled cards");
        }
        if (interaction.castAsCopies()) {
            gameData.interaction.clearAwaitingInput();
            Set<UUID> chosenIds = new HashSet<>(cardIds);
            for (UUID copyId : interaction.validCardIds()) {
                if (!chosenIds.contains(copyId)) {
                    gameData.removeFromExile(copyId);
                }
            }
            exileFreeCastQueueSupport.castChosenCopiesWithoutPaying(gameData, player.getId(), cardIds);
            return;
        }

        PendingInteraction.PortentOfCalamityState portentState =
                gameData.pollPendingInteraction(PendingInteraction.PortentOfCalamityState.class);
        if (portentState != null) {
            Set<UUID> chosenIds = new HashSet<>(cardIds);
            for (UUID revealedCardId : portentState.revealedCardIds()) {
                if (chosenIds.contains(revealedCardId)) {
                    continue;
                }
                ExiledCardEntry exiled = gameData.findExiledCard(revealedCardId);
                if (exiled != null && gameData.removeFromExile(revealedCardId)) {
                    gameData.playerHands.get(portentState.playerId()).add(exiled.card());
                }
            }
        }
        exileFreeCastQueueSupport.castChosenSpellsWithoutPaying(gameData, player, cardIds);
    }
}
