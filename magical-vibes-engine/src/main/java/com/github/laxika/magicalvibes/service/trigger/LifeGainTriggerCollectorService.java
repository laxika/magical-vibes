package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifeGainTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainMayPay(TriggerMatchContext match,
                                         MayPayManaEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE),
            @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_CONTROLLER_LOSES_LIFE)
    })
    private boolean handleLifeGainSequence(TriggerMatchContext match,
                                            SequenceEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain (sequence)", match.gameData().id, sourceCard.getName());
        return true;
    }
}
