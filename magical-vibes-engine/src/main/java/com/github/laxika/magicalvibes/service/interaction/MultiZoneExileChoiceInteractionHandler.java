package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles "exile any number of cards named X" choices spanning the target player's hand,
 * graveyard, and library (e.g. Memoricide-style effects). Card views are re-derived by the
 * same hand → graveyard → library scan the begin sites use; the answer (the actual exiling
 * and library shuffle) is applied by {@link ChoiceHandlerService#handleMultiZoneExileCardsChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiZoneExileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MultiZoneExileChoice> {

    private final ChoiceHandlerService choiceHandlerService;

    @Override
    public Class<PendingInteraction.MultiZoneExileChoice> handledType() {
        return PendingInteraction.MultiZoneExileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MultiZoneExileChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        choiceHandlerService.handleMultiZoneExileCardsChosen(gameData, player, cardIds);
    }
}
