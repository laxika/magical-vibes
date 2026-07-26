package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the Mirror of Fate choice: pick up to seven face-up exiled cards to put on top of
 * the library (the rest of the library is exiled). Card views are re-derived from the
 * player's exile zone at prompt time; the answer is applied by
 * {@link ExileSupport#handleMirrorOfFateChoice}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MirrorOfFateChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MirrorOfFateChoice> {

    private final ExileSupport exileSupport;

    @Override
    public Class<PendingInteraction.MirrorOfFateChoice> handledType() {
        return PendingInteraction.MirrorOfFateChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MirrorOfFateChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        exileSupport.handleMirrorOfFateChoice(gameData, player, cardIds);
    }
}
