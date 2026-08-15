package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BoostReferencedPermanentEffect boost = (BoostReferencedPermanentEffect) effect;
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        Permanent referenced = findReferencedPermanent(gameData, entry, boost.reference());
        if (referenced == null) {
            return;
        }

        var context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        referenced.setPowerModifier(referenced.getPowerModifier() + powerBoost);
        referenced.setToughnessModifier(referenced.getToughnessModifier() + toughnessBoost);

        gameLogService.append(gameData, GameLog.builder()
                .card(referenced.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                .build());
        log.info("Game {} - {} gets {}/{}", gameData.id, referenced.getCard().getName(), powerBoost, toughnessBoost);
    }

    private Permanent findReferencedPermanent(GameData gameData, StackEntry entry,
                                              PermanentReference reference) {
        return switch (reference) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
            case ATTACHED -> {
                Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
                yield source == null || !source.isAttached()
                        ? null
                        : findPermanent(gameData, source.getAttachedTo());
            }
        };
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }
}
