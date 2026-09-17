package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryWhenThisCreatureEnlistsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects Guardian of New Benalia's scry trigger after its Enlist ability triggers. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianOfNewBenaliaTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = ScryWhenThisCreatureEnlistsEffect.class, slot = EffectSlot.STATIC)
    private boolean handleScryTrigger(TriggerMatchContext match, ScryWhenThisCreatureEnlistsEffect effect,
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
                new ArrayList<>(List.of(new ScryEffect(2))),
                null,
                source.getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} scry trigger queued after enlisting", gameData.id, source.getCard().getName());
        return true;
    }
}
