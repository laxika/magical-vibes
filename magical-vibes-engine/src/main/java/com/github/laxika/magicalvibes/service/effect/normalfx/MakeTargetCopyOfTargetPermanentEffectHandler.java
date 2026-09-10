package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetPermanentEffect;
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
public class MakeTargetCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            log.info("Game {} - Permanent copy spell fizzles, insufficient targets", gameData.id);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent copySource = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (target == null || copySource == null) {
            log.info("Game {} - Permanent copy spell fizzles, a target left the battlefield", gameData.id);
            return;
        }

        String targetName = target.getCard().getName();
        permanentCopierService.applyCloneCopy(target, copySource, null, null);
        gameLogService.append(gameData, GameLog.textCardText(
                targetName + " becomes a copy of ", copySource.getCard(), "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, targetName,
                copySource.getCard().getName());
    }
}
