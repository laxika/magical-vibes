package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBlightsEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted player's blight action. */
@Component
@RequiredArgsConstructor
public class TargetPlayerBlightsEffectHandler implements NormalEffectHandlerBean {

    private final BlightEffectHandler blightEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerBlightsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerBlightsEffect targetBlight = (TargetPlayerBlightsEffect) effect;
        List<UUID> targets = entry.targetsForEffect(effect);
        UUID targetPlayerId = !targets.isEmpty() ? targets.getFirst() : entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        blightEffectHandler.resolveForPlayer(
                gameData, entry, new BlightEffect(targetBlight.count(), null), targetPlayerId);
    }
}
