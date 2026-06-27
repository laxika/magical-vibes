package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CloneService cloneService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPerm == null) {
            log.info("Game {} - Become-copy-until-end-of-turn target no longer exists", gameData.id);
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            log.info("Game {} - Become-copy-until-end-of-turn source no longer on battlefield", gameData.id);
            return;
        }

        if (!sourcePermanent.isCopyUntilEndOfTurn()) {
            sourcePermanent.setPreCopyCard(sourcePermanent.getCard());
        }

        String originalName = sourcePermanent.getCard().getName();
        cloneService.applyCloneCopy(sourcePermanent, targetPerm, null, null);
        sourcePermanent.setCopyUntilEndOfTurn(true);

        String targetName = targetPerm.getCard().getName();
        String logMsg = originalName + " becomes a copy of " + targetName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} becomes a copy of {} until end of turn", gameData.id, originalName, targetName);
    }
}
