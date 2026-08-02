package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles Struggle for Sanity's alternating hand exile
 * ({@link PendingInteraction.AlternatingHandExileChoice}). Both players see the target's remaining
 * hand and pick one card by index; the answer is applied by
 * {@link CardChoiceHandlerService#handleAlternatingHandExileChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlternatingHandExileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AlternatingHandExileChoice> {

    private final CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<PendingInteraction.AlternatingHandExileChoice> handledType() {
        return PendingInteraction.AlternatingHandExileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.AlternatingHandExileChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        cardChoiceHandlerService.handleAlternatingHandExileChosen(gameData, player, cardIndex);
    }
}
