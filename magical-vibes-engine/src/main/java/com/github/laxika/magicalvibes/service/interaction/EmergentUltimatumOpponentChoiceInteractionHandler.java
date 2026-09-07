package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies the opponent's Emergent Ultimatum choice and offers the remaining spells to the controller. */
@Component
@RequiredArgsConstructor
public class EmergentUltimatumOpponentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EmergentUltimatumOpponentChoice> {

    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.EmergentUltimatumOpponentChoice> handledType() {
        return PendingInteraction.EmergentUltimatumOpponentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EmergentUltimatumOpponentChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not the choosing opponent");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1 || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one card");
        }

        UUID shuffledCardId = cardIds.getFirst();
        Card shuffledCard = interaction.cards().stream()
                .filter(card -> card.getId().equals(shuffledCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid card ID: " + shuffledCardId));
        if (gameData.findExiledCard(shuffledCardId) == null) {
            throw new IllegalStateException("Chosen card is no longer exiled");
        }

        gameData.removeFromExile(shuffledCardId);
        gameData.playerDecks.computeIfAbsent(interaction.controllerId(), ignored -> new java.util.ArrayList<>())
                .add(shuffledCard);
        LibraryShuffleHelper.shuffleLibrary(gameData, interaction.controllerId());
        gameData.interaction.clearAwaitingInput();

        List<UUID> castableCardIds = interaction.cards().stream()
                .filter(card -> !card.getId().equals(shuffledCardId))
                .filter(this::isSpell)
                .map(Card::getId)
                .filter(cardId -> gameData.findExiledCard(cardId) != null)
                .toList();
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(interaction.playerId())
                        + " shuffles one Emergent Ultimatum card into its owner's library."));

        if (castableCardIds.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        interaction.controllerId(), castableCardIds, castableCardIds.size(),
                        "You may cast the other cards without paying their mana costs."));
    }

    private boolean isSpell(Card card) {
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
