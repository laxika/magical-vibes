package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getDeclaredTargetIds();
        if (targets.size() < 2 || !entry.isTargetLegal(0) || !entry.isTargetLegal(1)) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targets.get(0));
        ExiledCardEntry exiled = gameData.findExiledCard(targets.get(1));
        if (target == null || exiled == null || exiled.faceDown()
                || !gameData.exiledCardsWithMemoryCounters.contains(exiled.card().getId())
                || !exiled.card().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
            log.info("Game {} - The Animus copy ability has no legal targets", gameData.id);
            return;
        }

        String originalName = target.getCard().getName();
        if (!target.isCopyUntilControllerNextTurn()) {
            target.setUntilNextTurnPreCopyCard(target.getCard());
        }
        permanentCopierService.applyCloneCopy(target, exiled.card(), null, null, Set.of());
        target.setCopyUntilControllerNextTurn(true);
        target.setCopyUntilNextTurnControllerId(entry.getControllerId());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new MakeTargetCopyOfTargetCreatureUntilNextTurnEffect(),
                target.getId(), null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + exiled.card().getName()
                        + " until your next turn."));
    }
}
