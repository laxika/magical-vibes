package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the Brilliant Ultimatum play choice: the controller chooses any number of the chosen
 * pile's cards to play (lands onto the battlefield, spells cast without paying their mana costs).
 * The answer is applied by {@link BrilliantUltimatumSupport#playChosenCards}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrilliantUltimatumPlayChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.BrilliantUltimatumPlayChoice> {

    private final BrilliantUltimatumSupport brilliantUltimatumSupport;

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPlayChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPlayChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.BrilliantUltimatumPlayChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        brilliantUltimatumSupport.playChosenCards(gameData, player, cardIds);
    }
}
