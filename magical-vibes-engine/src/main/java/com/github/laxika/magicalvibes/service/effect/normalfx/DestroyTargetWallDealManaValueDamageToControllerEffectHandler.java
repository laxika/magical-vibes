package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetWallDealManaValueDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetWallDealManaValueDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetWallDealManaValueDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Snapshot the mana value and controller before destroying the Wall.
        int manaValue = target.getCard().getManaValue();
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), true);

        if (controllerId != null && manaValue > 0) {
            String cardName = entry.getCard().getName();
            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented."));
            } else {
                int rawDamage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
                damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
