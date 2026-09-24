package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMillAndRepeatIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves repeating token-and-mill effects such as Grist's +1 ability. */
@Component
@RequiredArgsConstructor
public class CreateTokenThenMillAndRepeatIfMilledEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final PutCountersOnSelfEffectHandler putCountersOnSelfEffectHandler;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenMillAndRepeatIfMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var repeatingEffect = (CreateTokenThenMillAndRepeatIfMilledEffect) effect;
        UUID controllerId = entry.getControllerId();

        while (true) {
            createTokenEffectHandler.resolve(gameData, entry, repeatingEffect.tokenEffect());

            List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, 1);
            if (milled == null || milled.isEmpty()) {
                return;
            }

            boolean matched = milled.stream().anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                    card,
                    repeatingEffect.repeatFilter(),
                    null,
                    gameData,
                    card.getOwnerId() != null ? card.getOwnerId() : controllerId));
            if (!matched) {
                return;
            }

            putCountersOnSelfEffectHandler.resolve(
                    gameData, entry, new PutCountersOnSelfEffect(CounterType.LOYALTY));
        }
    }
}
