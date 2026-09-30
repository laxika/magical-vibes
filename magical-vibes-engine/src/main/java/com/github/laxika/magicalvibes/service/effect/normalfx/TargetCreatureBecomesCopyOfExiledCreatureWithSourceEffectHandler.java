package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTargetId() == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        ExiledCardEntry exiled = gameData.getExiledWithPermanentEntries(
                        entry.getSourcePermanentId(), entry.getCard().getId()).stream()
                .filter(candidate -> !candidate.faceDown()
                        && candidate.card().hasType(CardType.CREATURE))
                .findFirst()
                .orElse(null);
        if (exiled == null) {
            log.info("Game {} - No creature card is exiled with the source", gameData.id);
            return;
        }

        String originalName = target.getCard().getName();
        permanentCopierService.applyCloneCopy(target, exiled.card(), null, null, Set.of());
        gameLogService.append(gameData,
                GameLog.textCardText(originalName + " becomes a copy of ", exiled.card(), "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, exiled.card().getName());
    }
}
