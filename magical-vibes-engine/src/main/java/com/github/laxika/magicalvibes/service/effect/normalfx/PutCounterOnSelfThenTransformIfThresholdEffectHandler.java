package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCounterOnSelfThenTransformIfThresholdEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSelfThenTransformIfThresholdEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnSelfThenTransformIfThresholdEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        int before = self.getCounterCount(e.counterType());
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), 1);
        int placed = self.getCounterCount(e.counterType()) - before;
        if (placed <= 0) return;
        String counterName = permanentCounterSupport.counterTypeName(e.counterType());

        // Check threshold and transform if met
        int currentCount = self.getCounterCount(e.counterType());

        if (currentCount >= e.threshold()) {
            if (e.optional()) {
                // "you may remove those counters and transform it" — put may ability on the stack
                gameData.queueMayAbility(
                        entry.getCard(),
                        entry.getControllerId(),
                        new MayEffect(
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(e.counterType()),
                                        new TransformSelfEffect()
                                ),
                                "Remove counters and transform?"
                        ),
                        null,
                        selfId
                );
            } else {
                permanentCounterSupport.removeCountersAndTransform(gameData, self, e.counterType(), counterName);
                // Append on-transform effects to the resolving entry so they are picked up
                // by the EffectResolutionService's for-loop (e.g. Treasure Map creates tokens)
                if (!e.onTransformEffects().isEmpty()) {
                    entry.getEffectsToResolve().addAll(e.onTransformEffects());
                }
            }
        }
    }
}
