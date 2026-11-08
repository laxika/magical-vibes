package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Queues triggers that care about the completion of a voting event. */
@Slf4j
@Service
@RequiredArgsConstructor
public class VotingFinishedTriggerCollectorService {

    private final GameLogService gameLogService;

    /**
     * "Whenever players finish voting": the ability goes on the stack as the card wrote it, with the vote as the players
     * finished it as the entry's triggering vote, which its effects read as they resolve (Erestor of the Council, Model
     * of Unity).
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_PLAYERS_FINISH_VOTING)
    private boolean handleVotingFinished(TriggerMatchContext match, CardEffect effect, TriggerContext context) {
        if (!(context instanceof TriggerContext.VotingFinished votingFinished)
                || match.permanent() == null) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setTriggeringVotingResult(votingFinished.result());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after voting finishes", match.gameData().id, sourceCard.getName());
        return true;
    }
}
