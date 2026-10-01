package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftTwiceFromSpellbookEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftTwiceSpellbookChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.DraftTwiceSpellbookChoice> {

    private final DraftTwiceFromSpellbookEffectHandler spellbookHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.GameLogService gameLogService;

    @Override
    public Class<PendingInteraction.DraftTwiceSpellbookChoice> handledType() {
        return PendingInteraction.DraftTwiceSpellbookChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.DraftTwiceSpellbookChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to draft");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1 || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one spellbook card");
        }

        Card chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid spellbook card"));

        gameData.interaction.clearAwaitingInput();
        gameData.addCardToHand(player.getId(), chosen);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " drafts ", chosen,
                " from " + interaction.sourceCardName() + "'s spellbook."));

        List<Card> draftedCards = new ArrayList<>(interaction.draftedCards());
        draftedCards.add(chosen);
        if (interaction.draftNumber() == 1) {
            List<Card> secondOffer = spellbookHandler.createOfferedCards(
                    player.getId(), interaction.spellbook());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DraftTwiceSpellbookChoice(
                    player.getId(), secondOffer, draftedCards, interaction.spellbook(),
                    interaction.sourceCardName(), 2));
            return;
        }

        List<UUID> draftedIds = draftedCards.stream().map(Card::getId).toList();
        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (draftedIds.contains(hand.get(i).getId())) {
                validIndices.add(i);
            }
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                player.getId(), player.getId(), validIndices, 1,
                false, false, List.of(), null,
                "Choose one of those cards to put onto the battlefield tapped.",
                false, false, false, null, null, 0, null,
                false, false, false, false,
                false, false, 0, false,
                null, null, new PutChosenCardFromHandOntoBattlefieldEffect(true), 0).withKeepInHand());
    }
}
