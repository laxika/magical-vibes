package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Renewed Solidarity's chosen-type end-step token-copy ability. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private static final PermanentEnteredBattlefieldThisTurnPredicate ENTERED_THIS_TURN =
            new PermanentEnteredBattlefieldThisTurnPredicate();
    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_COPY_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect();

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachTokenOfChosenSubtypeEnteredThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        if (sourcePermanent == null || sourcePermanent.getChosenSubtype() == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId)
                .withSourcePermanentSnapshot(sourcePermanent);
        PermanentHasSubtypePredicate chosenSubtype =
                new PermanentHasSubtypePredicate(sourcePermanent.getChosenSubtype());
        List<Card> sourceCards = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, ENTERED_THIS_TURN))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, chosenSubtype, filterContext))
                .map(Permanent::getCard)
                .toList();

        tokenCopySupport.createTokenCopies(
                gameData, entry, sourceCards, sourcePermanent, controllerId, TOKEN_COPY_PROFILE);
    }
}
