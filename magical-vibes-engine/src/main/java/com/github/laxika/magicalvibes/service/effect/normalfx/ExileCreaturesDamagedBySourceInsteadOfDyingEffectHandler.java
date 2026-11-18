package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileCreaturesDamagedBySourceInsteadOfDyingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesDamagedBySourceInsteadOfDyingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, source.getId(),
                Duration.UNTIL_END_OF_TURN));
        if (entry.getTargetId() != null) {
            gameData.exileDamagedCreaturesSourceIdsThisTurn.add(source.getId());
        }
    }
}
