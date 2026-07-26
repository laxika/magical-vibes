package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the two-stage Blackmail flow ({@link PendingInteraction.RevealCardsDiscardChoice}). In
 * the reveal stage the target player is shown their full hand and picks which cards to reveal; in
 * the discard stage the controller is shown only the revealed cards and picks one for the target to
 * discard. Both stages reuse the same pending-interaction family; prompt projection is centralized
 * and the answer is applied by {@link CardChoiceHandlerService#handleRevealCardsDiscardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsDiscardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealCardsDiscardChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.RevealCardsDiscardChoice> handledType() {
        return PendingInteraction.RevealCardsDiscardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.RevealCardsDiscardChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleRevealCardsDiscardChosen(gameData, player, cardIndex);
    }
}
