package com.github.laxika.magicalvibes.service.interaction;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
@Component
public class CommanderReturnChoiceInteractionHandler implements InteractionHandler<PendingInteraction.CommanderReturnChoice> {
    private final InputCompletionService completion;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyards;
    public CommanderReturnChoiceInteractionHandler(InputCompletionService completion, com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyards) { this.completion = completion; this.graveyards = graveyards; }
    public Class<PendingInteraction.CommanderReturnChoice> handledType() { return PendingInteraction.CommanderReturnChoice.class; }
    public Class<? extends InteractionAnswer> answerType() { return InteractionAnswer.MayAbilityChosen.class; }
    public void handleAnswer(GameData game, Player player, PendingInteraction.CommanderReturnChoice choice, InteractionAnswer answer) {
        if (!player.getId().equals(choice.playerId())) throw new IllegalArgumentException("Not your commander choice");
        if (((InteractionAnswer.MayAbilityChosen) answer).accepted()) {
            boolean removed;
            if (choice.fromZone() == Zone.EXILE) {
                removed = game.findExiledCard(choice.card().getId()) != null;
                if (removed) game.removeFromExile(choice.card().getId());
            } else {
                removed = game.playerGraveyards.get(choice.playerId()).removeIf(card -> card.getId().equals(choice.card().getId()));
                if (removed) graveyards.notifyCardsLeftGraveyard(game, choice.playerId(), choice.card());
            }
            if (removed) game.playerCommandZones.computeIfAbsent(choice.playerId(), id -> new ArrayList<>()).add(choice.card());
        }
        game.interaction.clearAwaitingInput();
        completion.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(game);
    }
}
