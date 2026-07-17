package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Phase 2 of Thieving Sprite: the caster chooses one of the target's revealed cards for the target to
 * discard. Only the revealed subset is shown (the rest of the hand stays hidden); the chosen index is
 * into that subset and applied by {@link CardChoiceHandlerService#handleRevealedCardToDiscardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseRevealedCardToDiscardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ChooseRevealedCardToDiscardChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.ChooseRevealedCardToDiscardChoice> handledType() {
        return PendingInteraction.ChooseRevealedCardToDiscardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.ChooseRevealedCardToDiscardChoice interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.revealedCards().stream().map(cardViewFactory::create).toList();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < cardViews.size(); i++) {
            validIndices.add(i);
        }
        sessionManager.sendToPlayer(recipientId, new ChooseFromRevealedHandMessage(
                cardViews, validIndices, interaction.prompt(), false));

        String playerName = gameData.playerIdToName.get(interaction.choosingPlayerId());
        log.info("Game {} - Awaiting {} to choose a revealed card to discard", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.ChooseRevealedCardToDiscardChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleRevealedCardToDiscardChosen(gameData, player, cardIndex);
    }
}
