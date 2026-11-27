package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BasicLandSearchQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibrarySearchSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "[Player] may search their library": yes searches (recorded as the search, CR 603.2; in a search queue the player
 * becomes one who searched this way) and shows the search, where finding nothing still counts (CR 701.23b); no goes on
 * without searching: to the queue's next search, or on with the resolution.
 */
@Component
@RequiredArgsConstructor
public class MaySearchLibraryChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MaySearchLibraryChoice> {

    private final LibrarySearchSupport librarySearchSupport;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<PendingInteraction.MaySearchLibraryChoice> handledType() {
        return PendingInteraction.MaySearchLibraryChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MaySearchLibraryChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to search");
        }
        boolean searches = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        UUID playerId = interaction.playerId();
        String playerName = gameData.playerIdToName.get(playerId);
        LibrarySearchParams search = interaction.search();
        LibrarySearchFollowUp followUp = search.followUp();
        gameData.interaction.clearAwaitingInput();

        if (searches) {
            // The search shown records itself, except one of a queue, whose searchers are known to the queue.
            LibrarySearchFollowUp.BasicLandSearchQueue queue = followUp.basicLandSearchQueue();
            if (queue != null && !queue.searchedPlayerIds().contains(playerId)) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
                search = search.withFollowUp(followUp.withBasicLandSearchQueue(queue.withSearchedPlayer(playerId)));
            }
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId, search, interaction.searchPrompt(),
                    true);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            // Nothing could be shown (the top cards an Aven Mindcensor leaves hold no match): the search is over.
            followUp = search.followUp();
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " chooses not to search their library."));
        }
        if (basicLandSearchQueueSupport.advance(gameData, followUp)) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
