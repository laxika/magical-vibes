package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the per-destroyed-permanent controller token rider from the controller tally recorded
 * on the stack entry by the preceding destroy effect.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenForEachDestroyedPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenForEachDestroyedPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createTokens = (CreateTokenForEachDestroyedPermanentControllerEffect) effect;
        Map<UUID, Integer> tokenCountsByController = new LinkedHashMap<>();
        for (UUID controllerId : entry.getEventPlayerIds()) {
            tokenCountsByController.merge(controllerId, 1, Integer::sum);
        }

        for (Map.Entry<UUID, Integer> tokensForController : tokenCountsByController.entrySet()) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData,
                    tokensForController.getKey(),
                    createTokens.tokenEffect().withAmount(tokensForController.getValue()),
                    entry.getCard().getSetCode()));
        }
    }
}
