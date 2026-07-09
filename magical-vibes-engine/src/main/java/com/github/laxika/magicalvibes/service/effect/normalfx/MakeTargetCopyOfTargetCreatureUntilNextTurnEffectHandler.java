package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
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

        String originalName = shapeshifter.getCard().getName();
        if (!shapeshifter.isCopyUntilControllerNextTurn()) {
            shapeshifter.setUntilNextTurnPreCopyCard(shapeshifter.getCard());
        }
        permanentCopierService.applyCloneCopy(shapeshifter, creature, null, null);
        shapeshifter.setCopyUntilControllerNextTurn(true);
        shapeshifter.setCopyUntilNextTurnControllerId(entry.getControllerId());

        String targetName = creature.getCard().getName();
        String logMsg = originalName + " becomes a copy of " + targetName + " until its controller's next turn.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} becomes a copy of {} until next turn", gameData.id, originalName, targetName);
    }
}
