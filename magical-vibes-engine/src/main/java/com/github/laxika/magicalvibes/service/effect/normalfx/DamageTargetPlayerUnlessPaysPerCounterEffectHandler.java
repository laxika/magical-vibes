package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "This permanent deals N damage to that player unless they pay {@code costPerCounter} for each
 * [counter] on it." The affected player is the stack entry's {@code targetId}; the pay-or-damage
 * prompt reuses the queue built by {@link EachPlayerTakesDamageUnlessPaysEffectHandler} with a
 * single-entry payer list.
 */
@Component
@RequiredArgsConstructor
public class DamageTargetPlayerUnlessPaysPerCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageTargetPlayerUnlessPaysPerCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageTargetPlayerUnlessPaysPerCounterEffect) effect;
        UUID payerId = entry.getTargetId();
        if (payerId == null || !gameData.playerIds.contains(payerId)) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        int counters = self.getCounterCount(e.counterType());
        if (counters <= 0) {
            return;
        }

        EachPlayerTakesDamageUnlessPaysEffect payOrDamage = new EachPlayerTakesDamageUnlessPaysEffect(
                e.damage(), e.costPerCounter().repeat(counters));
        eachPlayerTakesDamageUnlessPaysEffectHandler.offerToPlayers(
                gameData, entry, payOrDamage, List.of(payerId));
    }
}
