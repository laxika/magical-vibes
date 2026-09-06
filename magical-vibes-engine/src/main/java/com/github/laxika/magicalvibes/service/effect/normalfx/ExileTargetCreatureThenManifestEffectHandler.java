package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureThenManifestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Reality Shift's targeted exile and manifest effect. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureThenManifestEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final ManifestService manifestService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureThenManifestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null || !permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        permanentRemovalService.removeOrphanedAuras(gameData);
        manifestService.manifestTopCard(gameData, targetControllerId, entry.getCard());
    }
}
