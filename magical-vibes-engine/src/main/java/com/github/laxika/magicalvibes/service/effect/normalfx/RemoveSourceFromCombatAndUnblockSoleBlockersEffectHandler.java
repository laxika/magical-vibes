package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatAndUnblockSoleBlockersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveSourceFromCombatAndUnblockSoleBlockersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CombatRemovalSupport combatRemovalSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveSourceFromCombatAndUnblockSoleBlockersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        gameData.addEffectOnPermanent(entry, new CantBlockEffect(), source.getId(), Duration.UNTIL_END_OF_TURN);
        combatRemovalSupport.removeFromCombatAndUnblockSoleBlockers(gameData, entry, source);
    }
}
