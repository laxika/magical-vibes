package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BankEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

/**
 * Applies Bank only when the spell successfully resolves from its owner's hand.
 *
 * <p>The match lifecycle is not represented by the engine, so the resulting exile entry remains
 * available through a card-specific exile permission for the rest of the game state.</p>
 */
@Component
public class BankEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BankEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourceZone() == Zone.HAND
                && entry.getControllerId() != null
                && entry.getControllerId().equals(entry.getOwnerId())) {
            entry.setExileInsteadOfGraveyard(true);
            gameData.exilePlayPermissions.put(entry.getPhysicalCard().getId(), entry.getControllerId());
        }
    }
}
