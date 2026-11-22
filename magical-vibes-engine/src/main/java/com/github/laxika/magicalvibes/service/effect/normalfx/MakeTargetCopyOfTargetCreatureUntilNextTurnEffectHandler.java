package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeTargetCopyOfTargetCreatureUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCopyOfTargetCreatureUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            log.info("Game {} - Copy-until-next-turn ability fizzles, insufficient targets", gameData.id);
            return;
        }

        Permanent shapeshifter = gameQueryService.findPermanentById(gameData, targets.get(0));
        Permanent creature = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (shapeshifter == null || creature == null) {
            log.info("Game {} - Copy-until-next-turn ability fizzles, a target left the battlefield", gameData.id);
            return;
        }

        String originalName = FaceDownVisibility.publicName(gameData, shapeshifter);
        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, shapeshifter);
        permanentCopierService.applyCloneCopy(shapeshifter, creature, null, null);
        // CR 613.2a: a temporary copy is a layer-1 continuous effect with a duration. The card swap above applies
        // the copiable values; the floating effect keeps them and the CR 613.7 timestamp, and the duration clock
        // ends it at the ability controller's next turn.
        PermanentCopierService.beginCopy(gameData, entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), shapeshifter, preCopyCard,
                Duration.UNTIL_YOUR_NEXT_TURN);

        String targetName = FaceDownVisibility.publicName(gameData, creature);
        String logMsg = originalName + " becomes a copy of " + targetName + " until its controller's next turn.";
        gameLogService.append(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} becomes a copy of {} until next turn", gameData.id, originalName, targetName);
    }
}
