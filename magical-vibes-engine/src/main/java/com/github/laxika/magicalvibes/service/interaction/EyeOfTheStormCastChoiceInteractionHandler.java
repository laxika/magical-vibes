package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EyeOfTheStormCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EyeOfTheStormCastChoice> {

    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<PendingInteraction.EyeOfTheStormCastChoice> handledType() {
        return PendingInteraction.EyeOfTheStormCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EyeOfTheStormCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenCopyIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCopyIds.size() > interaction.validCopyIds().size()
                || new HashSet<>(chosenCopyIds).size() != chosenCopyIds.size()
                || !interaction.validCopyIds().containsAll(chosenCopyIds)
                || chosenCopyIds.stream().anyMatch(id -> gameData.findExiledCard(id) == null)) {
            throw new IllegalStateException("Choose distinct copies created by Eye of the Storm");
        }

        exileFreeCastQueueSupport.castChosenCopiesWithoutPaying(gameData, player.getId(), chosenCopyIds);
    }
}
