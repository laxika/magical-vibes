package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.HostileNegotiationsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the opponent's Hostile Negotiations pile choice. */
@Component
@RequiredArgsConstructor
public class HostileNegotiationsOpponentPileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HostileNegotiationsOpponentPileChoice> {

    private final HostileNegotiationsSupport hostileNegotiationsSupport;

    @Override
    public Class<PendingInteraction.HostileNegotiationsOpponentPileChoice> handledType() {
        return PendingInteraction.HostileNegotiationsOpponentPileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.HostileNegotiationsOpponentPileChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Hostile Negotiations choice");
        }
        boolean choosePile1 = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        gameData.interaction.clearAwaitingInput();
        hostileNegotiationsSupport.completeOpponentChoice(gameData, interaction, choosePile1);
    }
}
