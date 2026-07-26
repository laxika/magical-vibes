package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.PermanentAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Applies the current auction chooser's pick from the shared exiled pool through
 * {@link PermanentAuctionService#applyPick}, which advances the auction to the next player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentAuctionChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PermanentAuctionChoice> {

    private final PermanentAuctionService permanentAuctionService;

    @Override
    public Class<PendingInteraction.PermanentAuctionChoice> handledType() {
        return PendingInteraction.PermanentAuctionChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.PermanentAuctionChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        permanentAuctionService.applyPick(gameData, player, cardIds);
    }
}
