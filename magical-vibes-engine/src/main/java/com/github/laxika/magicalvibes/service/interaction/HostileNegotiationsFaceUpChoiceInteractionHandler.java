package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.HostileNegotiationsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the controller's choice of which Hostile Negotiations pile to turn face up. */
@Component
@RequiredArgsConstructor
public class HostileNegotiationsFaceUpChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.HostileNegotiationsFaceUpChoice> {

    private final HostileNegotiationsSupport hostileNegotiationsSupport;

    @Override
    public Class<PendingInteraction.HostileNegotiationsFaceUpChoice> handledType() {
        return PendingInteraction.HostileNegotiationsFaceUpChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.HostileNegotiationsFaceUpChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Hostile Negotiations choice");
        }
        boolean pile1FaceUp = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        gameData.interaction.clearAwaitingInput();
        hostileNegotiationsSupport.revealPileAndPromptOpponent(gameData, interaction, pile1FaceUp);
    }
}
