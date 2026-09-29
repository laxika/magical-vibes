package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceUpIfFaceDownEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnTargetCreatureFaceUpIfFaceDownEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TurnFaceUpCopyService turnFaceUpCopyService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnTargetCreatureFaceUpIfFaceDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TurnTargetCreatureFaceUpIfFaceDownEffect turnFaceUp =
                (TurnTargetCreatureFaceUpIfFaceDownEffect) effect;
        UUID targetId = entry.targetsForEffect(turnFaceUp).stream().findFirst()
                .orElse(entry.getTargetId());
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !target.isFaceDown() || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        turnFaceUpCopyService.turnFaceUpWithoutCost(gameData, target);
    }
}
