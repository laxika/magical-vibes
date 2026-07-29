package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect}: one life gain per
 * destroyed permanent, read off the positionally aligned {@code eventPlayerIds} /
 * {@code eventManaValues} channels the destroy-all handler stamped before destruction.
 */
@Component
@RequiredArgsConstructor
public class EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> controllerIds = entry.getEventPlayerIds();
        List<Integer> manaValues = entry.getEventManaValues();
        for (int i = 0; i < controllerIds.size() && i < manaValues.size(); i++) {
            int amount = manaValues.get(i);
            if (amount > 0) {
                lifeSupport.applyGainLife(gameData, controllerIds.get(i), amount,
                        entry.getCard().getName());
            }
        }
    }
}
