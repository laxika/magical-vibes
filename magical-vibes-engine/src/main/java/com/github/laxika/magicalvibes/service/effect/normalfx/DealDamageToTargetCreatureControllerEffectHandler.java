package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureControllerEffect) effect;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
