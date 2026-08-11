package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardCardsToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnTargetAndSacrificedCardsEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RegisterDelayedReturnTargetAndSacrificedCardsEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnTargetAndSacrificedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> cardIds = new ArrayList<>(2);
        if (entry.getTargetId() != null) {
            cardIds.add(entry.getTargetId());
        }
        if (entry.getSacrificedCardId() != null && !cardIds.contains(entry.getSacrificedCardId())) {
            cardIds.add(entry.getSacrificedCardId());
        }
        if (!cardIds.isEmpty()) {
            gameData.queueDelayedAction(new DelayedGraveyardCardsToBattlefieldUnderControl(
                    cardIds, entry.getControllerId()));
        }
    }
}
