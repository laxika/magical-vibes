package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the X announcement for a cast for an alternative mana cost containing {X}
 * (CR 601.2b), such as Entreat the Angels' miracle cost {X}{W}{W}. Unlike
 * {@link XValueChoiceInteractionHandler} there is no parked effect to re-run: the cast itself
 * continues in {@code MayCastHandlerService}, which charges the cost and puts the spell on the
 * stack carrying the announced X.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlternateCastXValueChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AlternateCastXValueChoice> {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<PendingInteraction.AlternateCastXValueChoice> handledType() {
        return PendingInteraction.AlternateCastXValueChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.AlternateCastXValueChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (chosenValue < 0 || chosenValue > interaction.maxValue()) {
            throw new IllegalArgumentException("X value must be between 0 and " + interaction.maxValue());
        }

        gameData.interaction.clearAwaitingInput();
        mayCastHandlerService.completeAlternateCastXChoice(gameData, player, interaction, chosenValue);
    }
}
