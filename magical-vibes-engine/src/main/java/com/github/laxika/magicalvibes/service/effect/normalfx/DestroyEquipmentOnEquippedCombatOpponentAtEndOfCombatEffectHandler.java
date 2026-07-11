package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    return;
                }
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target != null) {
                    gameData.queueDelayedAction(new DestroyEquipmentAtEndOfCombat(targetId));
                    String logEntry = "Equipment attached to " + target.getCard().getName()
                            + " will be destroyed at end of combat.";
                    gameData.gameLog.add(logEntry);
                }
    }
}
