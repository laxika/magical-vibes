package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LudevicCopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LudevicCopyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.LudevicCopyChoice> {

    private final GameQueryService gameQueryService;
    private final LudevicCopySupport ludevicCopySupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.LudevicCopyChoice> handledType() {
        return PendingInteraction.LudevicCopyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.LudevicCopyChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one creature card exiled with Ludevic");
        }

        Card chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(chosenIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is not available"));

        gameData.interaction.clearAwaitingInput();
        Permanent source = gameQueryService.findPermanentById(gameData, interaction.sourcePermanentId());
        if (source != null) {
            ludevicCopySupport.applyCopy(source, chosen, interaction.cards().size());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
