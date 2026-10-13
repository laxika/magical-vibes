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
import java.util.stream.Collectors;

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
        Set<UUID> permanentIdsBefore = permanentIdsOn(gameData, controllerId);

        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                1, FRACTAL, 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL));
        permanentControlSupport.applyCreateToken(
                gameData, controllerId, tokenEffect, entry.getCard().getSetCode());

        if (x <= 0) {
            return;
        }

        for (Permanent token : findNewFractalTokens(gameData, controllerId, permanentIdsBefore)) {
            if (gameQueryService.cantHaveCounters(gameData, token)) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, token, CounterType.PLUS_ONE_PLUS_ONE, x);
        }
    }

    private Set<UUID> permanentIdsOn(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield == null ? Set.of()
                : battlefield.stream().map(Permanent::getId).collect(Collectors.toSet());
    }

    /** Every Fractal token this effect created, so a replacement that makes several gets counters on each. */
    private List<Permanent> findNewFractalTokens(GameData gameData, UUID controllerId, Set<UUID> permanentIdsBefore) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> !permanentIdsBefore.contains(permanent.getId()))
                .filter(permanent -> permanent.getCard().isToken() && FRACTAL.equals(permanent.getCard().getName()))
                .toList();
    }
}
