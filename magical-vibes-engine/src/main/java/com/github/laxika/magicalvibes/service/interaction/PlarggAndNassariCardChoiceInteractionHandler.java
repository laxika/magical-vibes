package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the opponent's choice and starts the controller's up-to-two free-cast choice. */
@Component
@RequiredArgsConstructor
public class PlarggAndNassariCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PlarggAndNassariCardChoice> {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PlarggAndNassariCardChoice> handledType() {
        return PendingInteraction.PlarggAndNassariCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PlarggAndNassariCardChoice interaction,
                             InteractionAnswer answer) {
        if (!interaction.opponentId().equals(player.getId())) {
            throw new IllegalStateException("Not the choosing opponent");
        }
        List<UUID> selected = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (selected == null || selected.size() != 1 || !interaction.validCardIds().contains(selected.getFirst())) {
            throw new IllegalStateException("Choose one nonland card");
        }

        UUID excludedCardId = selected.getFirst();
        List<UUID> castableCardIds = interaction.validCardIds().stream()
                .filter(cardId -> !cardId.equals(excludedCardId))
                .filter(cardId -> {
                    var exiled = gameData.findExiledCard(cardId);
                    return exiled != null && isSpell(exiled.card());
                })
                .toList();

        gameData.interaction.clearAwaitingInput();
        if (castableCardIds.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        interaction.controllerId(), castableCardIds,
                        Math.min(interaction.maxCastCount(), castableCardIds.size()),
                        "You may cast up to two spells from among the other exiled cards without paying "
                                + "their mana costs."));
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType()
                || card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType);
    }
}
