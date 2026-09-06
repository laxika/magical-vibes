package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityFromSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.FirebenderAscensionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects Firebender Ascension's triggers when an attacking creature triggers its ability. */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebenderAscensionTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = FirebenderAscensionEffect.class, slot = EffectSlot.STATIC)
    private boolean handleFirebenderAscensionTrigger(TriggerMatchContext match,
                                                      FirebenderAscensionEffect effect,
                                                      TriggerContext context) {
        if (!(context instanceof TriggerContext.AttackingCreatureTriggeredAbility attack)) {
            return false;
        }

        StackEntry triggeredAbilitySnapshot = new StackEntry(attack.triggeredAbility());
        CardEffect copyEffect = new CopyTriggeredAbilityFromSnapshotEffect(triggeredAbilitySnapshot);
        List<CardEffect> effects = List.of(
                new PutCountersOnSourceCardEffect(CounterType.QUEST),
                new ConditionalEffect(
                        new SourceCounterThreshold(4, CounterType.QUEST),
                        new MayEffect(copyEffect, "Copy that triggered ability?")));

        Permanent source = match.permanent();
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggered ability queued for an attacking creature's triggered ability",
                gameData.id, source.getCard().getName());
        return true;
    }
}
