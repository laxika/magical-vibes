package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final WinGameEffectHandler winGameEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect conditional =
                (WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect) effect;
        UUID losingPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (losingPlayerId == null || controllerId == null || !gameQueryService
                .wasPlayerAttackedByControlledSubtypeThisTurn(gameData, losingPlayerId, controllerId,
                        conditional.subtype())) {
            return;
        }

        winGameEffectHandler.resolve(gameData, entry, new WinGameEffect());
    }
}
