package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithCostReductionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastExiledCardWithCostReductionEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCardWithCostReductionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastExiledCardWithCostReductionEffect castEffect = (MayCastExiledCardWithCostReductionEffect) effect;
        UUID cardId = entry.getTargetId();
        Card card = cardId == null ? null : gameQueryService.findCardInExileById(gameData, cardId);
        if (card == null) {
            return;
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                card,
                entry.getControllerId(),
                List.of(castEffect),
                "Cast " + card.getName() + " for {" + castEffect.genericCostReduction() + "} less?",
                cardId));
    }
}
