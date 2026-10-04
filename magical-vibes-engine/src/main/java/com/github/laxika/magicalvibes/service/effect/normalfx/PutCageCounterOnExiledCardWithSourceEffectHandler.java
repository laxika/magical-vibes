package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCageCounterOnExiledCardWithSourceEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Converts Mairsil's temporary source-tracked exile into a persistent cage-counter entry. */
@Component
public class PutCageCounterOnExiledCardWithSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCageCounterOnExiledCardWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID cardId = null;
        synchronized (gameData.exiledCards) {
            for (int i = gameData.exiledCards.size() - 1; i >= 0; i--) {
                ExiledCardEntry exiledCard = gameData.exiledCards.get(i);
                if (sourcePermanentId.equals(exiledCard.sourcePermanentId())) {
                    cardId = exiledCard.card().getId();
                    break;
                }
            }
        }
        if (cardId != null) {
            gameData.exiledCardsWithCageCounters.add(cardId);
            gameData.associateExiledCardWithSource(cardId, null);
        }
    }
}
