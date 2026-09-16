package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMillControllerAndRepeatIfMilledEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Grist's token-and-mill loop. */
@Component
@RequiredArgsConstructor
public class CreateTokenThenMillControllerAndRepeatIfMilledEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenMillControllerAndRepeatIfMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenThenMillControllerAndRepeatIfMilledEffect) effect;
        UUID controllerId = entry.getControllerId();

        while (true) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.tokenEffect(), entry.getCard().getSetCode()));

            List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, 1);
            boolean repeat = milled.stream().anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                    card, e.repeatPredicate(), entry.getCard().getId(), gameData, controllerId));
            if (!repeat) {
                return;
            }

            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, source, CounterType.LOYALTY, 1);
            }
        }
    }
}
