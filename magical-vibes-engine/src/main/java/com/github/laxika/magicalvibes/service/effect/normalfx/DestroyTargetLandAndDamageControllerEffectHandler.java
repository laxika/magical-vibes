package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetLandAndDamageControllerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetLandAndDamageControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetLandAndDamageControllerEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                if (!target.getCard().hasType(CardType.LAND)) {
                    String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
                    return;
                }

                // Find the controller of the targeted land before destruction
                UUID landControllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (landControllerId == null) {
                    return;
                }

                // Attempt to destroy the land
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Deal damage to the land's controller regardless of whether destruction succeeded
                destructionSupport.dealNoncombatDamageToPlayer(gameData, landControllerId, e.damage(),
                        entry.getCard().getName(), entry.getCard().getColor());

                gameOutcomeService.checkWinCondition(gameData);
    }
}
