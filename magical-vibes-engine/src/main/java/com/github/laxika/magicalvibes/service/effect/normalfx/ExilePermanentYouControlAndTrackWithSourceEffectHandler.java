package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
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
        if (sourcePermanentId == null
                || (exileEffect.untilSourceLeaves() && !sourceIsOnBattlefield(gameData, sourcePermanentId))) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> matchingIds = battlefield.stream()
                .filter(permanent -> matches(gameData, permanent, entry.getCard(), controllerId,
                        sourcePermanentId, exileEffect.filter()))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }
        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                exileAndQueueFollowUp(gameData, entry, permanent, sourcePermanentId,
                        entry.getCard(), exileEffect.thenEffect(), exileEffect.untilSourceLeaves());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PermanentYouControlToExile(
                        entry.getCard(), sourcePermanentId, controllerId, exileEffect.filter(),
                        exileEffect.thenEffect(), exileEffect.untilSourceLeaves()));
        playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                entry.getCard().getName() + " — choose a permanent to exile.");
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.PermanentYouControlToExile context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null
                && (!context.untilSourceLeaves() || sourceIsOnBattlefield(gameData, context.sourcePermanentId()))
                && context.controllerId().equals(gameQueryService.findPermanentController(gameData,
                permanentId))
                && matches(gameData, permanent, context.sourceCard(), context.controllerId(),
                context.sourcePermanentId(), context.filter())) {
            exileAndQueueFollowUp(gameData, gameData.pendingEffectResolutionEntry, permanent,
                    context.sourcePermanentId(), context.sourceCard(), context.thenEffect(),
                    context.untilSourceLeaves());
        }
    }

    /**
     * An "until this leaves the battlefield" exile does nothing once its source has already left
     * before the ability resolved (CR 610.3b). Separate enters/leaves trigger pairs (Wormfang Newt)
     * still exile, because their return is a different ability.
     */
    private boolean sourceIsOnBattlefield(GameData gameData, UUID sourcePermanentId) {
        return sourcePermanentId != null
                && gameQueryService.findPermanentById(gameData, sourcePermanentId) != null;
    }

    private void exileAndQueueFollowUp(GameData gameData, StackEntry entry, Permanent permanent,
                                       UUID sourcePermanentId, Card sourceCard, CardEffect thenEffect,
                                       boolean untilSourceLeaves) {
        int exiledPower = thenEffect == null ? 0 : gameQueryService.getEffectivePower(gameData, permanent);
        List<Card> leavingCards = List.copyOf(permanent.cardsLeavingBattlefield());
        if (!exileSupport.exilePermanentAndTrackWithSource(
                gameData, permanent, sourcePermanentId, sourceCard)) {
            return;
        }
        if (untilSourceLeaves) {
            for (Card leavingCard : leavingCards) {
                ExiledCardEntry exiled = leavingCard.isToken() ? null
                        : gameData.findExiledCard(leavingCard.getId());
                if (exiled != null) {
                    gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                            new PendingExileReturn(leavingCard, exiled.ownerId()));
                }
            }
        }
        if (thenEffect != null && entry != null) {
            entry.setEventValue(exiledPower);
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new QueueReflexiveAbilityEffect(thenEffect)));
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
