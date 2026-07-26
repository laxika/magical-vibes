package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Applies the opponent's Brilliant Ultimatum pile-one assignment. */
@Component
@RequiredArgsConstructor
public class BrilliantUltimatumPileSeparationChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.BrilliantUltimatumPileSeparationChoice> {

    private final BrilliantUltimatumSupport brilliantUltimatumSupport;

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPileSeparationChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPileSeparationChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(
            GameData gameData,
            Player player,
            PendingInteraction.BrilliantUltimatumPileSeparationChoice interaction,
            InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your pile assignment");
        }
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null
                || chosen.size() > interaction.validCardIds().size()
                || new HashSet<>(chosen).size() != chosen.size()
                || !interaction.validCardIds().containsAll(chosen)) {
            throw new IllegalArgumentException("Invalid Brilliant Ultimatum pile assignment");
        }

        gameData.interaction.clearAwaitingInput();
        brilliantUltimatumSupport.completePileSeparationStep1(gameData, chosen);
    }
}
