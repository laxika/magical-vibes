package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesPermanentToExileUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the non-targeting choice used by Wormfang Crab. The chosen card is tracked with the
 * source permanent so its leaves-the-battlefield ability can return it to its owner's control.
 */
@Component
@RequiredArgsConstructor
public class OpponentChoosesPermanentToExileUntilSourceLeavesEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesPermanentToExileUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (OpponentChoosesPermanentToExileUntilSourceLeavesEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = findSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        Card sourceCard = entry.getCard();
        if (opponents.size() == 1) {
            beginPermanentChoice(gameData, opponents.getFirst(), controllerId, sourceCard,
                    sourcePermanentId, exileEffect.filter());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentForPermanentExile(
                        sourceCard, sourcePermanentId, controllerId, exileEffect.filter()));
        playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                sourceCard.getName() + " — choose an opponent.");
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.ChooseOpponentForPermanentExile context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.controllerId())) {
            return;
        }
        beginPermanentChoice(gameData, chosenOpponentId, context.controllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.filter());
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.OpponentChoosesPermanentToExile context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null
                && context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                && matches(gameData, permanent, context)) {
            exileSupport.exilePermanentAndTrackWithSource(gameData, permanent,
                    context.sourcePermanentId(), context.sourceCard());
        }
    }

    private void beginPermanentChoice(GameData gameData, UUID choosingPlayerId, UUID controllerId,
                                      Card sourceCard, UUID sourcePermanentId,
                                      PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<UUID> matchingIds = battlefield.stream()
                .filter(permanent -> matches(gameData, permanent, sourceCard, controllerId,
                        sourcePermanentId, filter))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }
        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                exileSupport.exilePermanentAndTrackWithSource(gameData, permanent,
                        sourcePermanentId, sourceCard);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.OpponentChoosesPermanentToExile(
                        sourceCard, sourcePermanentId, choosingPlayerId, controllerId, filter));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, matchingIds,
                sourceCard.getName() + " — choose a permanent to exile.");
    }

    private boolean matches(GameData gameData, Permanent permanent,
                            PermanentChoiceContext.OpponentChoosesPermanentToExile context) {
        return matches(gameData, permanent, context.sourceCard(), context.controllerId(),
                context.sourcePermanentId(), context.filter());
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
