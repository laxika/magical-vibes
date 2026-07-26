package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprovisationCapstoneCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ImprovisationCapstoneCastChoice> {

    private final ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport;

    @Override
    public Class<PendingInteraction.ImprovisationCapstoneCastChoice> handledType() {
        return PendingInteraction.ImprovisationCapstoneCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ImprovisationCapstoneCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        improvisationCapstoneCastSupport.castChosenSpellsWithoutPaying(gameData, player, cardIds);
    }
}
