package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffectHandler implements NormalEffectHandlerBean {

    private static final String FRACTAL = "Fractal";

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int x = gameData.cardsDrawnThisTurn.getOrDefault(controllerId, 0);

        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                1, FRACTAL, 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL));
        permanentControlSupport.applyCreateToken(
                gameData, controllerId, tokenEffect, entry.getCard().getSetCode());

        if (x <= 0) {
            return;
        }

        Permanent token = findLastMatchingToken(gameData, controllerId);
        if (token == null || gameQueryService.cantHaveCounters(gameData, token)) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, token, CounterType.PLUS_ONE_PLUS_ONE, x);
    }

    private Permanent findLastMatchingToken(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        Permanent lastMatch = null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().isToken() && FRACTAL.equals(permanent.getCard().getName())) {
                lastMatch = permanent;
            }
        }
        return lastMatch;
    }
}
