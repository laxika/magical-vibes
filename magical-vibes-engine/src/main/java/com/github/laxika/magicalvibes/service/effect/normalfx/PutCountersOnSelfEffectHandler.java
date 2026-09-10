package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCountersOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnSelfEffect) effect;
        if (entry.getSourcePlanarObject() != null && entry.getSourcePermanentId() == null) {
            resolvePlanarSource(gameData, entry, e);
            return;
        }

        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        int count = e.amount() != null
                ? amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, self))
                : e.count();
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), count);
        if (entry.getSourcePermanentId() != null) {
            entry.setSourcePermanentSnapshot(new Permanent(self));
        }
    }

    private void resolvePlanarSource(GameData gameData, StackEntry entry, PutCountersOnSelfEffect effect) {
        PlanarObject source = gameData.planechase == null ? null
                : gameData.planechase.faceUp.stream()
                .filter(object -> object.getId().equals(entry.getSourcePlanarObject().getId()))
                .findFirst()
                .orElse(null);
        if (source == null) {
            source = entry.getSourcePlanarObject();
        }

        int count = effect.amount() != null
                ? amountEvaluationService.evaluate(gameData, effect.amount(),
                AmountContext.forStackEntry(entry, null))
                : effect.count();
        if (count <= 0) {
            return;
        }
        source.getCounters().merge(effect.counterType(), count, Integer::sum);
        entry.setSourcePlanarObject(source.copy());
    }
}
