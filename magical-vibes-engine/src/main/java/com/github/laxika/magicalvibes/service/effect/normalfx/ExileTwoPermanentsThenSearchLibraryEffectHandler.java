package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTwoPermanentsThenSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves two sequential non-targeting permanent choices followed by a library search. */
@Component
@RequiredArgsConstructor
public class ExileTwoPermanentsThenSearchLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTwoPermanentsThenSearchLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTwoPermanentsThenSearchLibraryEffect exileEffect =
                (ExileTwoPermanentsThenSearchLibraryEffect) effect;
        PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context = context(
                entry.getCard(), entry.getSourcePermanentId(), entry.getControllerId(), exileEffect, null);
        List<UUID> firstIds = matchingIds(gameData, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.firstFilter());
        List<UUID> secondIds = matchingIds(gameData, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.secondFilter());
        if (firstIds.isEmpty() || secondIds.isEmpty()) {
            return;
        }
        if (firstIds.size() == 1) {
            continueAfterFirstChoice(gameData, firstIds.getFirst(), context, entry);
            return;
        }
        beginChoice(gameData, context.controllerId(), firstIds, context,
                entry.getCard().getName() + " — choose a " + context.firstLabel() + " to exile.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context) {
        if (context.firstPermanentId() == null) {
            validateChoice(gameData, permanentId, context.controllerId(), context.sourceCard(),
                    context.sourcePermanentId(), context.firstFilter());
            continueAfterFirstChoice(gameData, permanentId, context, null);
            return;
        }
        completeBoth(gameData, context.firstPermanentId(), permanentId, context, null);
    }

    private void continueAfterFirstChoice(GameData gameData, UUID firstPermanentId,
                                          PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context,
                                          StackEntry resolvingEntry) {
        List<UUID> secondIds = matchingIds(gameData, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.secondFilter());
        if (secondIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary nextContext = context.withFirstPermanentId(
                firstPermanentId);
        if (secondIds.size() == 1) {
            completeBoth(gameData, firstPermanentId, secondIds.getFirst(), nextContext, resolvingEntry);
            return;
        }
        beginChoice(gameData, nextContext.controllerId(), secondIds, nextContext,
                nextContext.sourceCard().getName() + " — choose a " + nextContext.secondLabel() + " to exile.");
    }

    private void completeBoth(GameData gameData, UUID firstPermanentId, UUID secondPermanentId,
                              PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context,
                              StackEntry resolvingEntry) {
        validateChoice(gameData, firstPermanentId, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.firstFilter());
        validateChoice(gameData, secondPermanentId, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.secondFilter());

        Permanent first = gameQueryService.findPermanentById(gameData, firstPermanentId);
        if (first != null) {
            exileSupport.exilePermanentAndLog(gameData, first, context.sourceCard().getName());
        }
        if (!firstPermanentId.equals(secondPermanentId)) {
            Permanent second = gameQueryService.findPermanentById(gameData, secondPermanentId);
            if (second != null) {
                exileSupport.exilePermanentAndLog(gameData, second, context.sourceCard().getName());
            }
        }

        SearchLibraryEffect search = new SearchLibraryEffect(
                context.searchFilter(), LibrarySearchDestination.BATTLEFIELD);
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry != null) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(search));
        } else if (resolvingEntry != null) {
            searchLibraryEffectHandler.resolve(gameData, resolvingEntry, search);
        }
    }

    private void beginChoice(GameData gameData, UUID controllerId, List<UUID> ids,
                             PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context,
                             String prompt) {
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, ids, context, prompt);
    }

    private List<UUID> matchingIds(GameData gameData, UUID controllerId, Card sourceCard,
                                   UUID sourcePermanentId, PermanentPredicate filter) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId);
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, filter, filterContext))
                .map(Permanent::getId)
                .toList();
    }

    private void validateChoice(GameData gameData, UUID permanentId, UUID controllerId,
                                Card sourceCard, UUID sourcePermanentId, PermanentPredicate filter) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matches(gameData, permanent, sourceCard, controllerId, sourcePermanentId, filter)) {
            throw new IllegalStateException("Chosen permanent is no longer a matching permanent you control");
        }
    }

    private boolean matches(GameData gameData, Permanent permanent, Card sourceCard,
                            UUID controllerId, UUID sourcePermanentId, PermanentPredicate filter) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId);
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext);
    }

    private PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary context(
            Card sourceCard, UUID sourcePermanentId, UUID controllerId,
            ExileTwoPermanentsThenSearchLibraryEffect effect, UUID firstPermanentId) {
        return new PermanentChoiceContext.ExileTwoPermanentsThenSearchLibrary(
                sourceCard, sourcePermanentId, controllerId, effect.firstFilter(), effect.firstLabel(),
                effect.secondFilter(), effect.secondLabel(), effect.searchFilter(), firstPermanentId);
    }
}
