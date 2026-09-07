package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect) effect;
        List<UUID> targetIds = entry.targetsForGroup(copyEffect.targetGroup());
        List<UUID> copySourceIds = entry.targetsForGroup(copyEffect.copySourceGroup());
        if (targetIds.isEmpty() || copySourceIds.isEmpty()) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        Permanent copySource = gameQueryService.findPermanentById(gameData, copySourceIds.getFirst());
        if (target == null || copySource == null
                || !gameQueryService.isCreature(gameData, target)
                || !gameQueryService.isCreature(gameData, copySource)) {
            return;
        }

        String targetName = target.getCard().getName();
        if (!target.isCopyUntilEndOfTurn()) {
            target.setPreCopyCard(target.getCard());
        }
        permanentCopierService.applyCloneCopy(target, copySource, null, null);
        target.setCopyUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), target.getId(),
                entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + targetName + " a copy of "
                        + copySource.getCard().getName() + " until end of turn.")
                .build());
        log.info("Game {} - {} makes {} a copy of {} until end of turn", gameData.id,
                entry.getCard().getName(), targetName, copySource.getCard().getName());
    }
}
