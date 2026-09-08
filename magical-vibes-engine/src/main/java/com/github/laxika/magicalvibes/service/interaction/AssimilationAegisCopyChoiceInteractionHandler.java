package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssimilationAegisCopyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AssimilationAegisCopyChoice> {

    private final GameQueryService gameQueryService;
    private final AuraCopyService auraCopyService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.AssimilationAegisCopyChoice> handledType() {
        return PendingInteraction.AssimilationAegisCopyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.AssimilationAegisCopyChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one creature card exiled with Assimilation Aegis");
        }

        gameData.interaction.clearAwaitingInput();
        Permanent equipment = gameQueryService.findPermanentById(gameData, interaction.equipmentId());
        Permanent attached = equipment == null || equipment.getAttachedTo() == null
                ? null : gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        ExiledCardEntry chosen = gameData.findExiledCard(chosenIds.getFirst());
        if (equipment != null && attached != null
                && chosen != null
                && interaction.attachedCreatureId().equals(attached.getId())
                && AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler
                        .eligibleCards(gameData, equipment.getId(), equipment.getCard().getId())
                        .stream().anyMatch(entry -> entry.card().getId().equals(chosenIds.getFirst()))) {
            auraCopyService.applyExiledCreatureCopy(gameData, equipment, attached, chosen.card());
        }
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
