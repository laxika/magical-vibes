package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.WerewhatSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WerewhatOnEnterChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.WerewhatOnEnterChoice> {

    private final WerewhatSupport support;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InputCompletionService inputCompletionService;
    private final DrawService drawService;

    @Autowired
    public WerewhatOnEnterChoiceInteractionHandler(WerewhatSupport support,
                                                    @Lazy BattlefieldEntryService battlefieldEntryService,
                                                    InputCompletionService inputCompletionService,
                                                    @Lazy DrawService drawService) {
        this.support = support;
        this.battlefieldEntryService = battlefieldEntryService;
        this.inputCompletionService = inputCompletionService;
        this.drawService = drawService;
    }

    @Override
    public Class<PendingInteraction.WerewhatOnEnterChoice> handledType() {
        return PendingInteraction.WerewhatOnEnterChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.WerewhatOnEnterChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one valid creature card");
        }

        gameData.interaction.clearAwaitingInput();
        if (!chosenIds.isEmpty()) {
            WerewhatSupport.ChoiceResult result = support.applyChoice(gameData, interaction,
                    chosenIds.getFirst());
            if (result.fromHand()) {
                drawService.resolveDrawCard(gameData, interaction.controllerId());
            }
        }
        battlefieldEntryService.processCreatureETBEffects(gameData, interaction.controllerId(),
                interaction.card(), interaction.targetId(), interaction.wasCastFromHand(),
                interaction.etbMode(), interaction.kicked(), interaction.targetIds());
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
