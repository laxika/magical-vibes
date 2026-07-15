package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostEquippedCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEquippedCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostEquippedCreatureUntilEndOfTurnEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Equipment";

        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            log.info("Game {} - {} trigger fizzles: equipment no longer attached", gameData.id, sourceName);
            return;
        }
        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equippedCreature == null) {
            log.info("Game {} - {} trigger fizzles: equipped creature no longer on battlefield", gameData.id, sourceName);
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, equippedCreature);
        int powerBoost = amountEvaluationService.evaluate(gameData, e.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, e.toughnessBoost(), ctx);

        equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + powerBoost);
        equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + toughnessBoost);

        String logEntry = String.format("%s gets %+d/%+d until end of turn.",
                equippedCreature.getCard().getName(), powerBoost, toughnessBoost);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {}/{} until end of turn", gameData.id,
                equippedCreature.getCard().getName(), powerBoost, toughnessBoost);
    }
}
