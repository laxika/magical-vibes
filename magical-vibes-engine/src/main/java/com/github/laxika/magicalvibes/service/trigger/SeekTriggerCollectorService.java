package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicatesOfSoughtCardsAndManifestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeekTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = ConjureDuplicatesOfSoughtCardsAndManifestEffect.class,
            slot = EffectSlot.ON_CONTROLLER_SEEKS)
    private boolean handleSeek(TriggerMatchContext match, ConjureDuplicatesOfSoughtCardsAndManifestEffect effect,
                               TriggerContext context) {
        TriggerContext.Seek seek = (TriggerContext.Seek) context;
        if (seek.soughtCards().isEmpty()) {
            return false;
        }
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(new ConjureDuplicatesOfSoughtCardsAndManifestEffect(
                        seek.soughtCards()))),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on seek", match.gameData().id, sourceCard.getName());
        return true;
    }
}
