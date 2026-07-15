package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPerm == null) {
            log.info("Game {} - Mirrorweave target no longer exists", gameData.id);
            return;
        }

        // Snapshot the battlefield first: applying copies must not interleave with iteration.
        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getId().equals(targetId) && gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        });

        String targetName = targetPerm.getCard().getName();
        int count = 0;
        for (Permanent creature : creatures) {
            if (!creature.isCopyUntilEndOfTurn()) {
                creature.setPreCopyCard(creature.getCard());
            }
            permanentCopierService.applyCloneCopy(creature, targetPerm, null, null);
            creature.setCopyUntilEndOfTurn(true);
            // CR 613.2a: each temporary copy is a layer-1 continuous effect with a duration. The
            // card swap stores the copiable values; the floating effect carries the timestamp and
            // drives the revert (BecomeCopyOfTargetCreatureUntilEndOfTurnEffect) at cleanup.
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), creature.getId(),
                    entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                    creature.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
            count++;
        }

        String logMsg = entry.getCard().getName() + " makes " + count
                + " other creature(s) a copy of " + targetName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - Mirrorweave copies {} onto {} creatures", gameData.id, targetName, count);
    }
}
