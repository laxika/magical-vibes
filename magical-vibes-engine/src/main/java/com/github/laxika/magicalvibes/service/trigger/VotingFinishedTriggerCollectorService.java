package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ErestorOfTheCouncilEffect;
import com.github.laxika.magicalvibes.model.effect.ModelOfUnityEffect;
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

    @CollectsTrigger(value = ErestorOfTheCouncilEffect.class, slot = EffectSlot.ON_PLAYERS_FINISH_VOTING)
    private boolean handleErestor(TriggerMatchContext match, ErestorOfTheCouncilEffect trigger,
                                  TriggerContext context) {
        if (!(context instanceof TriggerContext.VotingFinished votingFinished)
                || match.permanent() == null) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        ErestorOfTheCouncilEffect snapshotEffect =
                new ErestorOfTheCouncilEffect(votingFinished.result());
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(snapshotEffect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after voting finishes", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = ModelOfUnityEffect.class, slot = EffectSlot.ON_PLAYERS_FINISH_VOTING)
    private boolean handleModelOfUnity(TriggerMatchContext match, ModelOfUnityEffect trigger,
                                       TriggerContext context) {
        if (!(context instanceof TriggerContext.VotingFinished votingFinished)
                || match.permanent() == null) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        ModelOfUnityEffect snapshotEffect = new ModelOfUnityEffect(votingFinished.result());
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(snapshotEffect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after voting finishes", match.gameData().id, sourceCard.getName());
        return true;
    }
}
