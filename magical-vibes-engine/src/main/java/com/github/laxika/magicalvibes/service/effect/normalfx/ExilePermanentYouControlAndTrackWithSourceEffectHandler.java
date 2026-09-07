package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a non-targeting choice of one permanent controlled by the ability's controller. */
@Component
@RequiredArgsConstructor
public class ExilePermanentYouControlAndTrackWithSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExilePermanentYouControlAndTrackWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExilePermanentYouControlAndTrackWithSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = findSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> matchingIds = battlefield.stream()
                .filter(permanent -> matches(gameData, permanent, entry.getCard(), controllerId,
                        sourcePermanentId, exileEffect.filter()))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }
        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                exileSupport.exilePermanentAndTrackWithSource(gameData, permanent,
                        sourcePermanentId, entry.getCard());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PermanentYouControlToExile(
                        entry.getCard(), sourcePermanentId, controllerId, exileEffect.filter()));
        playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                entry.getCard().getName() + " — choose a permanent to exile.");
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.PermanentYouControlToExile context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null
                && context.controllerId().equals(gameQueryService.findPermanentController(gameData,
                permanentId))
                && matches(gameData, permanent, context.sourceCard(), context.controllerId(),
                context.sourcePermanentId(), context.filter())) {
            exileSupport.exilePermanentAndTrackWithSource(gameData, permanent,
                    context.sourcePermanentId(), context.sourceCard());
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

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.getCard() == entry.getCard())
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);
    }
}
