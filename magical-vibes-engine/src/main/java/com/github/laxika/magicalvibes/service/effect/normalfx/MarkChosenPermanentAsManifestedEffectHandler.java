package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MarkChosenPermanentAsManifestedEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the manifest state after the shared hand-card choice puts the card onto the battlefield. */
@Component
@RequiredArgsConstructor
public class MarkChosenPermanentAsManifestedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MarkChosenPermanentAsManifestedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        if (permanent == null || !permanent.isFaceDown()) {
            return;
        }

        permanent.setManifested(true);
        battlefieldEntryService.processFaceDownCreatureETBTriggers(
                gameData, entry.getControllerId(), permanent.getCard());
    }
}
