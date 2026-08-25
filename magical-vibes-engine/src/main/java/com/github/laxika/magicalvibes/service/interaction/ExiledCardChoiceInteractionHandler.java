package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles a mandatory choice among matching face-up cards in exile. */
@Component
@RequiredArgsConstructor
public class ExiledCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledCardChoice> {

    private final InputCompletionService inputCompletionService;
    private final ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffectHandler returnHandler;

    @Override
    public Class<PendingInteraction.ExiledCardChoice> handledType() {
        return PendingInteraction.ExiledCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1 || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose one exiled card named " + interaction.cardName());
        }

        ExiledCardEntry exiled = returnHandler.findMatchingEntry(
                gameData, cardIds.getFirst(), interaction.cardName());
        if (exiled == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        returnHandler.returnToBattlefield(gameData, exiled);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
