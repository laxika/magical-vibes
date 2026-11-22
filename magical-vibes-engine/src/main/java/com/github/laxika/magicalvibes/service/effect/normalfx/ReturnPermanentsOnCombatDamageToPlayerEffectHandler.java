package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnPermanentsOnCombatDamageToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final BounceSupport bounceSupport;
    private final ValidTargetService validTargetService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentsOnCombatDamageToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.RETURN, StepOutcome.NONE);
        if (entry.getTargetIds().isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + " chooses not to return any permanents."));
            return;
        }
        List<Permanent> legal = new ArrayList<>();
        for (UUID targetId : entry.getTargetIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent == null || !entry.getAttackedTargetId().equals(
                    gameQueryService.findPermanentController(gameData, targetId))
                    || !validTargetService.isValidTriggeredAbilityPermanentTarget(gameData,
                    entry.getCard(), List.of(effect), TargetFilters.permanent(), permanent, entry.getControllerId())) continue;
            legal.add(permanent);
        }
        bounceSupport.returnChosenTogether(gameData, entry, legal);
    }
}
