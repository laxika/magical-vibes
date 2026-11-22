package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileAllPermanentsEffect) effect;
        List<Permanent> toExile = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withXValue(entry.getXValue());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toExile.add(perm);
                }
            }
        });

        // The step's result, each permanent as it was before any of them left (CR 608.2h).
        Map<UUID, LastKnownCardStats> lastKnown = new HashMap<>();
        for (Permanent perm : toExile) {
            lastKnown.put(perm.getId(), new LastKnownCardStats(perm.getId(),
                    gameQueryService.getEffectivePower(gameData, perm),
                    gameQueryService.getEffectiveToughness(gameData, perm),
                    gameQueryService.getPermanentManaValue(perm),
                    gameQueryService.getEffectiveName(gameData, perm),
                    gameQueryService.findPermanentController(gameData, perm.getId())));
        }
        List<UUID> exiled = new ArrayList<>();
        List<UUID> exiledControllers = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        List<LastKnownCardStats> known = new ArrayList<>();
        UUID sourcePermanentId = e.trackWithSource()
                ? entry.getSourcePermanentId() != null
                        ? entry.getSourcePermanentId()
                        : findSourcePermanentId(gameData, entry)
                : null;

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            gameData.runAsSingleCardExileBatch(() -> permanentRemovalService.performSimultaneousRemovals(gameData, toExile, () -> {
            for (Permanent perm : toExile) {
                List<Card> leavingCards = new ArrayList<>(perm.cardsLeavingBattlefield());
                List<Card> cardsExiled = ExileTargetPermanentEffectHandler.leavingCards(perm);
                boolean removed;
                if (sourcePermanentId != null) {
                    removed = permanentRemovalService.removePermanentToExile(gameData, perm, sourcePermanentId);
                } else {
                    removed = permanentRemovalService.removePermanentToExile(gameData, perm);
                }
                if (removed) {
                    LastKnownCardStats asItWas = lastKnown.get(perm.getId());
                    exiled.add(perm.getId());
                    known.add(asItWas);
                    if (asItWas.controllerId() != null) {
                        exiledControllers.add(asItWas.controllerId());
                    }
                    ExileTargetPermanentEffectHandler.addMovedCards(gameData, perm.getId(), cardsExiled, moved);
                }
                for (Card exiledCard : leavingCards) {
                    UUID exiledCardId = exiledCard.getId();
                    if (e.perpetualCastCostIncrease() != 0) {
                        gameData.perpetualGenericCastCostIncreases.merge(
                                exiledCardId, e.perpetualCastCostIncrease(), Integer::sum);
                    }
                    if (e.perpetualEnterTapped()) {
                        gameData.perpetualEnterTappedCardIds.add(exiledCardId);
                    }
                    if (e.ownerMayPlayWhileExiled()) {
                        UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCardId);
                        if (ownerId != null) {
                            exileSupport.grantPlayWhileExiled(gameData, exiledCardId, ownerId);
                        }
                    }
                    if (e.controllerMayPlayWhileExiled()
                            && removed
                            && gameData.findExiledCard(exiledCardId) != null) {
                        exileSupport.grantPlayWhileExiled(gameData, exiledCardId, entry.getControllerId());
                        if (e.controllerMaySpendAnyManaType()) {
                            gameData.updateExilePlayPermission(exiledCardId, granted -> granted.withAnyManaType(true));
                        }
                    }
                }
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}",
                        gameData.id, perm.getCard().getName(), entry.getCard().getName());
            }
            }));
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        entry.setEventValue(toExile.size());
        entry.recordStep(StepKind.EXILE,
                StepOutcome.ofObjects(exiled, exiledControllers, moved).withLastKnownPermanents(known));

        if (e.returnOneAtEachUpkeep() && e.trackWithSource() && !toExile.isEmpty()) {
            registerUpkeepReturn(gameData, entry, sourcePermanentId);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * "For as long as any of those cards remain exiled, at the beginning of each player's upkeep, that player returns
     * one of the exiled cards they own" (Dimensional Breach): a delayed triggered ability of this resolution, its
     * controller the spell's (CR 603.7e), that triggers at every upkeep while one of the cards it exiled is still
     * exiled (CR 603.7b).
     */
    private void registerUpkeepReturn(GameData gameData, StackEntry entry, UUID sourcePermanentId) {
        List<UUID> exiledIds = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> !card.isToken()).map(Card::getId).toList();
        if (exiledIds.isEmpty()) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.ANY),
                        ReturnCardExiledWithSourceToBattlefieldEffect.ownedByActivePlayer()),
                sourcePermanentId, null, DelayedReferents.NONE,
                new CardsStillInZone(Zone.EXILE, gameData.exileEntryVersionsOf(exiledIds))));
    }

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return entry.getCard().getId();
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return entry.getCard().getId();
    }
}
