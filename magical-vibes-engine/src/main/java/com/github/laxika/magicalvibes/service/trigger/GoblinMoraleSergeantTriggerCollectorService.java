package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedNontokenCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects Goblin Morale Sergeant's "whenever this enlists a nontoken creature" ability: its trigger-condition record's
 * {@code triggeredEffect()} goes on the stack with the enlisted creature as the entry's triggering permanent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoblinMoraleSergeantTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsCombatTrigger(value = ConjureDuplicateOfEnlistedNontokenCreatureEffect.class,
            watch = CombatWatch.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS)
    private boolean handleConjureTrigger(TriggerMatchContext match,
                                         ConjureDuplicateOfEnlistedNontokenCreatureEffect effect,
                                         TriggerContext context) {
        if (!(context instanceof TriggerContext.AttackingCreatureTriggeredAbility attack)
                || !attack.enlistment()
                || match.permanent() == null
                || !match.permanent().getId().equals(attack.attackingCreature().getId())
                || attack.enlistedCreature() == null
                || attack.enlistedCreature().getCard().isToken()) {
            return false;
        }

        Permanent source = match.permanent();
        Permanent enlisted = attack.enlistedCreature();
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s enlist trigger",
                new ArrayList<>(List.of(effect.triggeredEffect())),
                null,
                source.getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        // "That creature": the enlisted creature, read as the ability resolves (else as it last existed).
        entry.setTriggeringPermanentId(enlisted.getId());
        entry.setTriggeringPermanentControllerId(match.controllerId());
        entry.setTriggeringPermanent(enlisted);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} conjure trigger queued after enlisting {}",
                gameData.id, source.getCard().getName(), enlisted.getCard().getName());
        return true;
    }
}
