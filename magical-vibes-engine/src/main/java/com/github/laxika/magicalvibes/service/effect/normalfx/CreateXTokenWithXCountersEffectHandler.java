package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateXTokenWithXCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateXTokenWithXCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateXTokenWithXCountersEffect) effect;
        int xValue = entry.getXValue();
        if (xValue < 0) {
            return;
        }

        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                e.tokenName(), e.power(), e.toughness(),
                e.color(), e.colors(), e.subtypes());
        permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());

        if (xValue == 0) {
            return;
        }

        Permanent token = findLastMatchingToken(gameData, entry.getControllerId(), e.tokenName());
        if (token == null || gameQueryService.cantHaveCounters(gameData, token)) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, token, e.counterType(), xValue);
    }

    private Permanent findLastMatchingToken(GameData gameData, UUID controllerId, String tokenName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        Permanent lastMatch = null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().isToken() && tokenName.equals(permanent.getCard().getName())) {
                lastMatch = permanent;
            }
        }
        return lastMatch;
    }
}
