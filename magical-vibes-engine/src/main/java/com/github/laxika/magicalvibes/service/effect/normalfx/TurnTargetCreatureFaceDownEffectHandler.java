package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnTargetCreatureFaceDownEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnTargetCreatureFaceDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TurnTargetCreatureFaceDownEffect turnFaceDown = (TurnTargetCreatureFaceDownEffect) effect;
        UUID targetId = entry.targetsForEffect(turnFaceDown).stream().findFirst()
                .orElse(entry.getTargetId());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || target.isFaceDown()) {
            return;
        }
        target.setCard(target.getOriginalCard());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
    }
}
