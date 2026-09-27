package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayExiledCostCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

/** Grants the ability controller permission to play the card exiled to pay that ability. */
@Component
public class AllowPlayExiledCostCardThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayExiledCostCardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getExiledCostCardSnapshot() == null
                || gameData.findExiledCard(entry.getExiledCostCardSnapshot().getId()) == null) {
            return;
        }
        gameData.exilePlayPermissions.put(entry.getExiledCostCardSnapshot().getId(), entry.getControllerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(entry.getExiledCostCardSnapshot().getId());
    }
}
