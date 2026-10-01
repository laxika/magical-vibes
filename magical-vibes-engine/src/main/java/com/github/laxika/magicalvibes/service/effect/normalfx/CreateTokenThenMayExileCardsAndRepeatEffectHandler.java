package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMayExileCardsAndRepeatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves token creation followed by the optional graveyard-exile repeat loop. */
@Component
@RequiredArgsConstructor
public class CreateTokenThenMayExileCardsAndRepeatEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenMayExileCardsAndRepeatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var repeat = (CreateTokenThenMayExileCardsAndRepeatEffect) effect;
        UUID controllerId = entry.getControllerId();

        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, controllerId, repeat.tokenEffect(), entry.getCard().getSetCode()));

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        long matchingCards = graveyard == null ? 0 : graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, repeat.exileFilter(), sourceCardId))
                .count();
        if (matchingCards < repeat.exileCount()) {
            return;
        }

        CardEffect nextIteration = new CreateTokenThenMayExileCardsAndRepeatEffect(
                repeat.tokenEffect(), repeat.exileCount(), repeat.exileFilter());
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(new MayEffect(
                new ExileNCardsFromGraveyardThenEffect(
                        repeat.exileCount(), repeat.exileFilter(), nextIteration),
                "Exile " + repeat.exileCount()
                        + " cards from your graveyard to repeat this process?")));
    }
}
