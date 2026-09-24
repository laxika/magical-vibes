package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantExileInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Records the current opposing creature and planeswalker identities for perpetual exile on death. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantExileInsteadOfDyingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantExileInsteadOfDyingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(entry.getControllerId())
                    || permanent.getOriginalCard() == null
                    || (!gameQueryService.isCreature(gameData, permanent)
                    && !gameQueryService.isPlaneswalker(gameData, permanent))) {
                return;
            }
            gameData.perpetualExileInsteadOfDyingCardIds.add(permanent.getOriginalCard().getId());
        });
    }
}
