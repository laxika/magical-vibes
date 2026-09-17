package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES)
    private boolean handleFinalChapterResolution(TriggerMatchContext match, CardEffect trigger,
                                                  TriggerContext context) {
        if (!(context instanceof TriggerContext.SagaFinalChapterAbilityResolved resolved)
                || !match.controllerId().equals(resolved.sagaControllerId())) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after a final Saga chapter resolves",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
