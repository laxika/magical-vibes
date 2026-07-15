package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
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
    private final PermanentCopierService permanentCopierService;

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
        permanentCopierService.applyCloneCopy(sourcePermanent, targetPerm, null, null);
        sourcePermanent.setCopyUntilEndOfTurn(true);
        // CR 613.2a: a temporary copy is a layer-1 continuous effect with a duration. The card
        // swap above stores the copiable values; the floating effect carries the CR 613.7
        // timestamp and drives the revert when it expires at the cleanup step.
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                entry.getControllerId(), effect, sourcePermanentId, null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));

        String targetName = targetPerm.getCard().getName();
        String logMsg = originalName + " becomes a copy of " + targetName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} becomes a copy of {} until end of turn", gameData.id, originalName, targetName);
    }
}
