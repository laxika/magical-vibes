package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AradeshTheFounderEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects Aradesh's reward trigger after a creature enlists. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AradeshTheFounderTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = AradeshTheFounderEffect.class, slot = EffectSlot.STATIC)
    private boolean handleEnlistedCreature(TriggerMatchContext match, AradeshTheFounderEffect effect,
                                            TriggerContext context) {
        if (!(context instanceof TriggerContext.AttackingCreatureTriggeredAbility attack)
                || !attack.enlistment()
                || match.permanent() == null) {
            return false;
        }

        Permanent source = match.permanent();
        GameData gameData = match.gameData();
        List<CardEffect> effects = List.of(
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT),
                new ConditionalEffect(new TriggeringPermanentPowerAtLeast(4), new DrawCardEffect(1)));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId());
        entry.setTriggeringPermanentId(attack.attackingCreature().getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        int stackSizeBeforeReward = gameData.stack.size();
        gameData.enqueueTrigger(entry);
        int enlistTriggerIndex = gameData.stack.indexOf(attack.triggeredAbility());
        if (enlistTriggerIndex >= 0 && gameData.stack.size() > stackSizeBeforeReward) {
            List<StackEntry> queuedRewards = new ArrayList<>(
                    gameData.stack.subList(stackSizeBeforeReward, gameData.stack.size()));
            gameData.stack.subList(stackSizeBeforeReward, gameData.stack.size()).clear();
            gameData.stack.addAll(enlistTriggerIndex, queuedRewards);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} reward trigger queued for an enlisted creature",
                gameData.id, source.getCard().getName());
        return true;
    }
}
