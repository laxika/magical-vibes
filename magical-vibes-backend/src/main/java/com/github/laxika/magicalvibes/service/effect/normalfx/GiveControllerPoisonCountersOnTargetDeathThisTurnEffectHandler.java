package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerPoisonCountersOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiveControllerPoisonCountersOnTargetDeathThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveControllerPoisonCountersOnTargetDeathThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GiveControllerPoisonCountersOnTargetDeathThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, delayed poison trigger not registered", gameData.id);
            return;
        }

        gameData.creatureGivingControllerPoisonOnDeathThisTurn.merge(
                target.getCard().getId(), e.amount(), Integer::sum);

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, its controller gets {} poison counter(s)",
                gameData.id, target.getCard().getName(), e.amount());
    }
}
