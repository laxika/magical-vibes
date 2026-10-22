package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.WerewhatSupport;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class WerewhatBackFaceChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.WerewhatBackFaceChoice> {

    private final WerewhatSupport support;
    private final WerewhatOnEnterChoiceInteractionHandler onEnterHandler;

    public WerewhatBackFaceChoiceInteractionHandler(WerewhatSupport support,
                                                    @Lazy WerewhatOnEnterChoiceInteractionHandler onEnterHandler) {
        this.support = support;
        this.onEnterHandler = onEnterHandler;
    }

    @Override
    public Class<PendingInteraction.WerewhatBackFaceChoice> handledType() {
        return PendingInteraction.WerewhatBackFaceChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.WerewhatBackFaceChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        int faceIndex = interaction.options().indexOf(((InteractionAnswer.ListChoiceMade) answer).choice());
        if (faceIndex < 0) {
            throw new IllegalStateException("Invalid face choice");
        }

        gameData.interaction.clearAwaitingInput();
        WerewhatSupport.ChoiceResult result = support.applyChoice(gameData, interaction.origin(),
                interaction.selectedCardId(), faceIndex == 1);
        if (result.fromHand()) {
            onEnterHandler.drawForHandExile(gameData, interaction.origin());
        }
        onEnterHandler.finishEntering(gameData, interaction.origin());
    }
}
