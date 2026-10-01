package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlThenCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a non-targeting choice to exile a controlled permanent and create a token. */
@Component
@RequiredArgsConstructor
public class ExilePermanentYouControlThenCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExilePermanentYouControlThenCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExilePermanentYouControlThenCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> matchingIds = battlefield.stream()
                .filter(permanent -> matches(gameData, permanent, entry.getCard(), controllerId,
                        exileEffect.filter()))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }
        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                exileAndCreate(gameData, entry, permanent, entry.getCard(), controllerId,
                        exileEffect.token());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ExilePermanentYouControlThenCreateToken(
                        entry.getCard(), controllerId, exileEffect.filter(), exileEffect.token()));
        playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                entry.getCard().getName() + " — choose a permanent to exile.");
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.ExilePermanentYouControlThenCreateToken context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matches(gameData, permanent, context.sourceCard(), context.controllerId(), context.filter())) {
            return;
        }

        exileAndCreate(gameData, gameData.pendingEffectResolutionEntry, permanent,
                context.sourceCard(), context.controllerId(), context.token());
    }

    private void exileAndCreate(GameData gameData, StackEntry entry, Permanent permanent,
                                Card sourceCard, UUID controllerId,
                                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect token) {
        exileSupport.exilePermanentAndLog(gameData, permanent, sourceCard.getName());
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, controllerId, token, sourceCard.getSetCode());
        if (entry != null) {
            entry.getCreatedPermanentIds().addAll(createdIds);
        }
    }

    private boolean matches(GameData gameData, Permanent permanent, Card sourceCard,
                            UUID controllerId, PermanentPredicate filter) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(controllerId);
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext);
    }
}
