package com.github.laxika.magicalvibes.service.interaction;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.CommanderZoneMoveService;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class CommanderReplacementChoiceInteractionHandler implements InteractionHandler<PendingInteraction.CommanderReplacementChoice> {
    private final InputCompletionService completion;
    private final CommanderZoneMoveService moves;
    private final com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggers;
    public CommanderReplacementChoiceInteractionHandler(InputCompletionService completion, CommanderZoneMoveService moves, com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggers) { this.completion = completion; this.moves = moves; this.triggers = triggers; }
    public Class<PendingInteraction.CommanderReplacementChoice> handledType() { return PendingInteraction.CommanderReplacementChoice.class; }
    public Class<? extends InteractionAnswer> answerType() { return InteractionAnswer.MayAbilityChosen.class; }
    public void handleAnswer(GameData game, Player player, PendingInteraction.CommanderReplacementChoice choice, InteractionAnswer answer) {
        CommanderZoneMove move = game.pendingCommanderZoneMoves.stream().filter(candidate -> candidate.card().getId().equals(choice.move().card().getId()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Commander move is no longer pending"));
        if (!player.getId().equals(move.ownerId())) throw new IllegalArgumentException("Not your commander choice");
        game.pendingCommanderZoneMoves.remove(move);
        game.interaction.clearAwaitingInput();
        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        if (accepted) game.playerCommandZones.computeIfAbsent(move.ownerId(), id -> new ArrayList<>()).add(move.card());
        else {
            game.completingCommanderZoneMove = move.card().getId();
            try {
                if (move.destination() == Zone.HAND) game.addCardToHand(move.ownerId(), move.card());
                else {
                    List<Card> library = game.playerDecks.get(move.ownerId());
                    library.add(Math.min(Math.max(0, move.libraryIndex()), library.size()), move.card());
                    if (move.shuffle()) Collections.shuffle(library);
                }
            } finally { game.completingCommanderZoneMove = null; }
        }
        var bounce = game.commanderBounceContexts.remove(move.card().getId());
        if (bounce != null) {
            triggers.checkEnchantedPermanentLTBTriggers(game, bounce.permanent(), bounce.controllerId(), accepted ? Zone.COMMAND : Zone.HAND);
            if (!accepted) {
                triggers.checkControllerCreatureReturnedToHandTriggers(game, bounce.permanent(), bounce.wasCreature(), bounce.ownerId());
                triggers.checkControllerAnotherNonlandPermanentReturnedToHandTriggers(game, bounce.permanent(), bounce.controllerId());
                triggers.checkControllerPermanentReturnedToHandTriggers(game, bounce.ownerId());
                triggers.checkPermanentReturnedToHandTriggers(game, bounce.ownerId(), bounce.permanent());
                game.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn.add(bounce.ownerId());
            }
        }
        if (!moves.beginPending(game)) completion.processMayAbilitiesThenAutoPassPreservingPriority(game);
    }
}
