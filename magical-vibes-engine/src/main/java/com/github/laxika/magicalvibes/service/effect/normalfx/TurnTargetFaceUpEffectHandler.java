package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnTargetFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnTargetFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !target.isFaceDown() || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, target);
    }
}
