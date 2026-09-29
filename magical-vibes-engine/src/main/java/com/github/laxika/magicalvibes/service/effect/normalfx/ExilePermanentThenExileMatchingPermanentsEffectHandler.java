package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentThenExileMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a non-targeting choice followed by a global permanent exile. */
@Component
@RequiredArgsConstructor
public class ExilePermanentThenExileMatchingPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExilePermanentThenExileMatchingPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExilePermanentThenExileMatchingPermanentsEffect exileEffect =
                (ExilePermanentThenExileMatchingPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> matches(gameData, permanent, entry.getCard(), controllerId,
                        entry.getSourcePermanentId(), exileEffect.choiceFilter()))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }

        if (matchingIds.size() == 1) {
            exileChosenAndMatching(gameData, matchingIds.getFirst(), new PermanentChoiceContext
                    .ExilePermanentThenExileMatchingPermanents(
                            entry.getCard(), entry.getSourcePermanentId(), controllerId,
                            exileEffect.choiceFilter(), exileEffect.matchingFilter(),
                            exileEffect.choiceLabel()));
            return;
        }

        PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context =
                new PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents(
                        entry.getCard(), entry.getSourcePermanentId(), controllerId,
                        exileEffect.choiceFilter(), exileEffect.matchingFilter(),
                        exileEffect.choiceLabel());
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds, context,
                entry.getCard().getName() + " — choose a " + exileEffect.choiceLabel() + " to exile.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matches(gameData, chosen, context.sourceCard(), context.controllerId(),
                        context.sourcePermanentId(), context.choiceFilter())) {
            throw new IllegalStateException("Chosen permanent is no longer a matching permanent you control");
        }
        exileChosenAndMatching(gameData, permanentId, context);
    }

    private void exileChosenAndMatching(GameData gameData, UUID chosenId,
                                        PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
        if (chosen == null) {
            return;
        }
        exileSupport.exilePermanentAndLog(gameData, chosen, context.sourceCard().getName());

        List<Permanent> matchingPermanents = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields
                        .getOrDefault(playerId, List.of()).stream())
                .filter(permanent -> !permanent.getId().equals(chosenId)
                        && matches(gameData, permanent, context.sourceCard(), context.controllerId(),
                        context.sourcePermanentId(), context.matchingFilter()))
                .toList();
        for (Permanent permanent : matchingPermanents) {
            Permanent current = gameQueryService.findPermanentById(gameData, permanent.getId());
            if (current != null) {
                exileSupport.exilePermanentAndLog(gameData, current, context.sourceCard().getName());
            }
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
}
