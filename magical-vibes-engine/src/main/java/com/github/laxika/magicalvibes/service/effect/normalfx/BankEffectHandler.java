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
 * <p>A banked card may only be cast from exile in a later game of the match, never in the same
 * game, so no exile cast permission is granted. The engine doesn't model multi-game matches.</p>
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
        }
    }
}
