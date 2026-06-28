package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentAndDamageControllerIfDestroyedEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndDamageControllerIfDestroyedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentAndDamageControllerIfDestroyedEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                // Find the controller of the targeted permanent before destruction
                UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (targetControllerId == null) {
                    return;
                }

                // Attempt to destroy the permanent
                boolean destroyed = destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Deal damage only if the permanent was actually put into a graveyard
                if (destroyed) {
                    destructionSupport.dealNoncombatDamageToPlayer(gameData, targetControllerId, e.damage(),
                            entry.getCard().getName(), entry.getCard().getColor());
                }

                gameOutcomeService.checkWinCondition(gameData);
    }
}
