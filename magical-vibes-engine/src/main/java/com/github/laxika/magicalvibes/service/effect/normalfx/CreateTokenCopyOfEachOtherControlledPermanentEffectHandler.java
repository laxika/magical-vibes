package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachOtherControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a token copy of each matching permanent the controller controls, other than the source, as one ordered
 * token creation event through {@link TokenCopySupport} (CR 616.1): the copies of every permanent are created at the
 * same time, so the creating player orders the doublers and additions once for all of them.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachOtherControlledPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TokenCopySupport tokenCopySupport;
    private final CreateTokenCopyOfTargetPermanentEffectHandler targetCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachOtherControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfEachOtherControlledPermanentEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Card> sourceCards = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (permanent.getId().equals(entry.getSourcePermanentId())) {
                continue;
            }
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, copyEffect.filter(), filterContext)) {
                sourceCards.add(targetCopyHandler.copySourceFor(permanent));
            }
        }

        tokenCopySupport.createTokenCopies(gameData, entry, sourceCards, null, copyEffect.copyEffect());
    }
}
