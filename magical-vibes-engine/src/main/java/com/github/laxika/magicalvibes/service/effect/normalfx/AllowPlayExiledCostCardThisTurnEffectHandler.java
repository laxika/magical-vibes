package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayExiledCostCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Grants the ability controller permission to play the card exiled to pay that ability. */
@Component
public class AllowPlayExiledCostCardThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayExiledCostCardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID exiledCardId = entry.getExiledCostCardSnapshot() != null
                ? entry.getExiledCostCardSnapshot().getId()
                : entry.getActivatedAbilityExiledCardIds().size() == 1
                ? entry.getActivatedAbilityExiledCardIds().getFirst()
                : null;
        if (exiledCardId == null || gameData.findExiledCard(exiledCardId) == null) {
            return;
        }
        gameData.exilePlayPermissions.put(exiledCardId, entry.getControllerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiledCardId);
    }
}
