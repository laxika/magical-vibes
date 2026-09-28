package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects Benalish Knight-Counselor's one-time boon after it enlists a creature. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenalishKnightCounselorTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect.class,
            slot = EffectSlot.STATIC)
    private boolean handleBoonTrigger(TriggerMatchContext match,
                                      GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect effect,
                                      TriggerContext context) {
        if (!(context instanceof TriggerContext.AttackingCreatureTriggeredAbility attack)
                || !attack.enlistment()
                || match.permanent() == null
                || !match.permanent().getId().equals(attack.attackingCreature().getId())) {
            return false;
        }

        Permanent source = match.permanent();
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s enlist trigger",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} one-time boon queued after enlisting", gameData.id, source.getCard().getName());
        return true;
    }
}
