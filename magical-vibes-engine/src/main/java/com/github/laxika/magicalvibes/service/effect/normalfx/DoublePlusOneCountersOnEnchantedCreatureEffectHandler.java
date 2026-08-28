package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoublePlusOneCountersOnEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoublePlusOneCountersOnEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attachedPermanentId = attachedPermanentId(gameData, entry);
        if (attachedPermanentId == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, attachedPermanentId);
        if (enchantedCreature == null) {
            return;
        }

        int current = enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (current > 0) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, enchantedCreature, CounterType.PLUS_ONE_PLUS_ONE, current);
        }
    }

    private UUID attachedPermanentId(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && source.isAttached()) {
            return source.getAttachedTo();
        }
        Permanent attached = entry.getAttachedPermanentSnapshot();
        if (attached != null) {
            return attached.getId();
        }
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        return sourceSnapshot != null && sourceSnapshot.isAttached()
                ? sourceSnapshot.getAttachedTo()
                : null;
    }
}
