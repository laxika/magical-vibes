package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a library search that lets the player put the revealed card into hand or graveyard. */
@Component
@RequiredArgsConstructor
public class LibrarySearchDestinationChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.LibrarySearchDestinationChoice> {

    private static final String HAND = PendingInteraction.LibrarySearchDestinationChoice.OPTIONS.getFirst();

    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<PendingInteraction.LibrarySearchDestinationChoice> handledType() {
        return PendingInteraction.LibrarySearchDestinationChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.LibrarySearchDestinationChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        String choice = ((InteractionAnswer.ListChoiceMade) answer).choice();
        boolean toHand = HAND.equalsIgnoreCase(choice);
        boolean toGraveyard = PendingInteraction.LibrarySearchDestinationChoice.OPTIONS.getLast()
                .equalsIgnoreCase(choice);
        if (!toHand && !toGraveyard) {
            throw new IllegalStateException("Invalid library search destination: " + choice);
        }

        libraryChoiceHandlerService.handleLibrarySearchDestinationChosen(
                gameData, player, interaction.card(), toHand);
    }
}
