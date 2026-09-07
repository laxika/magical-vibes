package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnTargetCreatureFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TurnFaceUpCopyService turnFaceUpCopyService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnTargetCreatureFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TurnTargetCreatureFaceUpEffect turnFaceUp = (TurnTargetCreatureFaceUpEffect) effect;
        UUID targetId = entry.targetsForEffect(turnFaceUp).stream().findFirst()
                .orElse(entry.getTargetId());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !target.isFaceDown()) {
            return;
        }
        turnFaceUpCopyService.turnFaceUpWithoutCost(gameData, target);
    }
}
