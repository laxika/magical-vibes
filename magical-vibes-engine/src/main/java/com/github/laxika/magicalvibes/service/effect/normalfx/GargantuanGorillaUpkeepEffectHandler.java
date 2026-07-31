package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GargantuanGorillaUpkeepEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You may sacrifice a Forest. If you sacrifice a snow Forest this way, this creature gains trample
 * until end of turn. If you don't sacrifice a Forest, sacrifice this creature and it deals 7 damage
 * to you." With no Forest to sacrifice there is no choice to make, so the penalty applies without a
 * prompt.
 */
@Component
@RequiredArgsConstructor
public class GargantuanGorillaUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GargantuanGorillaUpkeepSupport gargantuanGorillaUpkeepSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GargantuanGorillaUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (gargantuanGorillaUpkeepSupport.forestIds(gameData, controllerId).isEmpty()) {
            gargantuanGorillaUpkeepSupport.applyPenalty(
                    gameData, controllerId, entry.getSourcePermanentId(), entry.getCard());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect),
                entry.getCard().getName() + " - Sacrifice a Forest?",
                null, null, entry.getSourcePermanentId()));
    }
}
