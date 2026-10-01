package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Destroys a target permanent and records a perpetual boost for its card identity. */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenPerpetuallyBoostEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (DestroyTargetPermanentThenPerpetuallyBoostEffect) effect;
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        var card = target.getCard();
        entry.rememberLastKnownPermanentCard(target.getId(), card);
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
        PerpetualCardPowerToughnessSupport.remember(gameData, card, boost.powerBoost(), boost.toughnessBoost());
    }
}
