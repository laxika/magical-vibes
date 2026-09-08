package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ReselectTargetAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TargetRedirectionSupport targetRedirectionSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReselectTargetAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTriggeringCardId();
        if (targetCardId == null) {
            return;
        }

        StackEntry targetEntry = gameQueryService.findStackEntryByCardId(gameData, targetCardId);
        if (targetEntry == null || !targetEntry.isSingleTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        List<UUID> legalTargets = targetRedirectionSupport.collectValidTargetsIncludingCurrent(gameData, targetEntry);
        if (legalTargets.isEmpty()) {
            return;
        }

        UUID selectedTarget = legalTargets.get(ThreadLocalRandom.current().nextInt(legalTargets.size()));
        if (selectedTarget.equals(targetEntry.getTargetId())) {
            return;
        }

        targetEntry.setTargetId(selectedTarget);
        gameLogService.append(gameData,
                GameLog.cardTextCard(entry.getCard(), " reselects the target of ", targetEntry.getCard(), " at random."));

        if (targetEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData, targetEntry);
        } else {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetEntry);
        }
    }
}
