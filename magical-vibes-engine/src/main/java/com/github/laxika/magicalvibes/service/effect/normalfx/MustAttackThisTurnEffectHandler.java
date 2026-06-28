package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MustAttackThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setMustAttackThisTurn(true);

        if (e.forceAttackController()) {
            // Force the creature to attack the ability's controller specifically, not their planeswalkers
            // (Scryfall ruling: "it must attack you, not the planeswalker")
            target.setMustAttackTargetId(entry.getControllerId());

            String controllerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = target.getCard().getName() + " must attack " + controllerName + " this turn if able.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} must attack {} this turn if able", gameData.id, target.getCard().getName(), controllerName);
        } else {
            String logEntry = target.getCard().getName() + " must attack this turn if able.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} must attack this turn if able", gameData.id, target.getCard().getName());
        }
    }
}
