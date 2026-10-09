package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final EquipSupport equipSupport;
    @org.springframework.beans.factory.annotation.Autowired
    private AnimationSupport animationSupport;
    @org.springframework.beans.factory.annotation.Autowired
    private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.clearReturnedPermanentIds();
        entry.setEventValue(0);
        var returnEffect = (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
        if (returnEffect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            resolveFromAllGraveyards(gameData, entry, returnEffect);
        } else {
            resolveForController(gameData, entry, effect, entry.getControllerId());
        }
    }

    private void resolveFromAllGraveyards(GameData gameData, StackEntry entry,
                                          ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        List<GraveyardCard> cardsToReturn = new ArrayList<>();
        int totalManaValue = 0;
        int maxTotalManaValue = effect.maxTotalManaValue();
        if (effect.dynamicMaxTotalManaValue() != null) {
            maxTotalManaValue = Math.max(0, amountEvaluationService.evaluate(
                    gameData, effect.dynamicMaxTotalManaValue(), AmountContext.forStackEntry(entry, null)));
        }
        for (UUID targetCardId : targets(entry, effect)) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
            if (graveyardOwnerId == null) {
                continue;
            }
            Card card = gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of()).stream()
                    .filter(graveyardCard -> graveyardCard.getId().equals(targetCardId))
                    .findFirst().orElse(null);
            if (card != null && predicateEvaluationService.matchesCardPredicate(
                    card, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId,
                    null, null, entry.getXValue())
                    && (!effect.hasTotalManaValueCap()
                    || totalManaValue + card.getManaValue() <= maxTotalManaValue)) {
                cardsToReturn.add(new GraveyardCard(graveyardOwnerId, card));
                totalManaValue += card.getManaValue();
            }
        }

        if (cardsToReturn.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (GraveyardCard graveyardCard : cardsToReturn) {
                Card card = graveyardCard.card();
                if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(permanent, effect.grantColor(), effect.grantSubtype());
                if (effect.enterTapped()) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardCard.ownerId());
                applyEntryAnimation(gameData, entry, permanent, effect);
                UUID battlefieldControllerId = effect.underOwnersControl()
                        ? graveyardCard.ownerId() : controllerId;
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, battlefieldControllerId, permanent, enterTappedTypes, simultaneouslyEntered,
                        enteringCounters(effect));
                simultaneouslyEntered.add(permanent);
                entry.rememberReturnedPermanent(permanent.getId());
                applyReturnRiders(gameData, permanent, effect);
                returnedCards.add(card);
            }
            for (Permanent permanent : simultaneouslyEntered) {
                UUID battlefieldControllerId = effect.underOwnersControl()
                        ? permanent.getEnteredFromGraveyardOwnerId() : controllerId;
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                        gameData, battlefieldControllerId, permanent, permanent.getCard());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!returnedCards.isEmpty()) {
            entry.setEventValue(returnedCards.size());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " returns " + returnedCards.size()
                            + " card(s) from graveyards to the battlefield."));
        }
    }

    public void resolveForController(GameData gameData, StackEntry entry, CardEffect effect,
                                     UUID graveyardOwnerId) {
        resolveForController(gameData, entry, effect, graveyardOwnerId, targets(entry, effect));
    }

    private List<UUID> targets(StackEntry entry, CardEffect effect) {
        List<UUID> graveyardTargets = entry.getTargetCardIdsForEffect(effect);
        if (!graveyardTargets.isEmpty() || entry.getTargetCardIdsByEffect().containsKey(effect)) {
            return graveyardTargets;
        }
        List<UUID> bound = entry.targetsForBoundEffectGroup(effect);
        if (bound != null) return bound;
        Integer group = entry.getResolvingEffectTargetGroup();
        return group != null ? entry.targetsForGroup(group) : entry.targetsForEffect(effect);
    }

    public void resolveForController(GameData gameData, StackEntry entry, CardEffect effect,
                                     UUID graveyardOwnerId, List<UUID> targetCardIds) {
        var e = (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
        if (e.randomlyReturnTwoAndPutRestOnBottom()) {
            resolveRandomTwoAndPutRestOnBottom(gameData, entry, e, graveyardOwnerId, targetCardIds);
            return;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null || graveyard.isEmpty() || targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        Set<UUID> trackedIds = e.fromBattlefieldThisTurn()
                ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(graveyardOwnerId, Set.of())
                : null;
        List<Card> cardsToReturn = new ArrayList<>();
        int totalManaValue = 0;
        int maxTotalManaValue = e.maxTotalManaValue();
        if (e.dynamicMaxTotalManaValue() != null) {
            maxTotalManaValue = Math.max(0, amountEvaluationService.evaluate(
                    gameData, e.dynamicMaxTotalManaValue(), AmountContext.forStackEntry(entry, null)));
        }
        for (UUID targetCardId : targetCardIds) {
            Card card = graveyard.stream()
                    .filter(graveyardCard -> graveyardCard.getId().equals(targetCardId))
                    .findFirst().orElse(null);
            if (card != null
                    && (trackedIds == null || trackedIds.contains(card.getId()))
                    && predicateEvaluationService.matchesCardPredicate(card, e.filter(), entry.getCard().getId(),
                    gameData, graveyardOwnerId, null, null, entry.getXValue())
                    && (!e.hasTotalManaValueCap()
                    || totalManaValue + card.getManaValue() <= maxTotalManaValue)) {
                cardsToReturn.add(card);
                totalManaValue += card.getManaValue();
            }
        }

        if (cardsToReturn.isEmpty()) {
            return;
        }

        if (!e.attachToSourceHost() && cardsToReturn.stream().anyMatch(Card::isAura)) {
            List<Permanent> preparedPermanents = new ArrayList<>();
            for (Card card : cardsToReturn) {
                Permanent prepared = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(prepared, e.grantColor(), e.grantSubtype());
                if (e.enterTapped()) prepared.tap();
                prepared.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                applyEntryAnimation(gameData, entry, prepared, e);
                applyReturnRiders(gameData, prepared, e);
                preparedPermanents.add(prepared);
                entry.rememberReturnedPermanent(prepared.getId());
            }
            graveyardReturnSupport.returnPreparedPermanentsWithAuraChoices(
                    gameData, graveyardOwnerId, preparedPermanents, enteringCounters(e));
            return;
        }

        Set<CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Permanent sourceHost = e.attachToSourceHost() ? sourceHost(gameData, entry) : null;
        List<Permanent> equipmentToAttach = new ArrayList<>();
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : cardsToReturn) {
                if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }
                if (e.attachToSourceHost() && card.isAura()
                        && (sourceHost == null
                        || !auraAttachmentService.canEnchant(gameData, card, graveyardOwnerId, sourceHost))) {
                    continue;
                }
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(permanent, e.grantColor(), e.grantSubtype());
                if (e.enterTapped()) {
                    permanent.tap();
                }
                if (e.attachToSourceHost() && card.isAura()) {
                    permanent.setAttachedTo(sourceHost.getId());
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                applyEntryAnimation(gameData, entry, permanent, e);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, graveyardOwnerId, permanent, enterTappedTypes, simultaneouslyEntered,
                        enteringCounters(e));
                simultaneouslyEntered.add(permanent);
                entry.rememberReturnedPermanent(permanent.getId());
                if (e.attachToSourceHost() && !card.isAura()) {
                    equipmentToAttach.add(permanent);
                }
                applyReturnRiders(gameData, permanent, e);
                returnedCards.add(card);
            }
            for (Permanent permanent : simultaneouslyEntered) {
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                        gameData, graveyardOwnerId, permanent, permanent.getCard());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (sourceHost != null) {
            for (Permanent equipment : equipmentToAttach) {
                equipSupport.attachEquipment(gameData, equipment, sourceHost);
            }
        }

        if (!returnedCards.isEmpty()) {
            entry.setEventValue(returnedCards.size());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(graveyardOwnerId) + " returns " + returnedCards.size()
                            + " card(s) from the graveyard to the battlefield."));
        }
    }

    private void applyEntryAnimation(GameData gameData, StackEntry entry, Permanent permanent,
                                     ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        var animation = effect.entryAnimation();
        if (animation == null || (effect.entryAnimationCondition() != null
                && !conditionEvaluationService.isMet(gameData, effect.entryAnimationCondition(),
                com.github.laxika.magicalvibes.service.effect.ConditionContext.forStackEntry(entry)))) return;
        AmountContext context = AmountContext.forStackEntry(entry, permanent);
        int power = animation.power() == null ? permanent.getBasePower()
                : amountEvaluationService.evaluate(gameData, animation.power(), context);
        int toughness = animation.toughness() == null ? permanent.getBaseToughness()
                : amountEvaluationService.evaluate(gameData, animation.toughness(), context);
        animationSupport.animatePermanently(gameData, permanent, animation, power, toughness,
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId());
    }

    private void resolveRandomTwoAndPutRestOnBottom(
            GameData gameData, StackEntry entry,
            ReturnTargetCardsFromGraveyardToBattlefieldEffect effect,
            UUID graveyardOwnerId, List<UUID> targetCardIds) {
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null || graveyard.isEmpty() || targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> legalTargets = targetCardIds.stream()
                .map(targetId -> graveyard.stream()
                        .filter(card -> card.getId().equals(targetId))
                        .findFirst().orElse(null))
                .filter(card -> card != null
                        && predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), entry.getCard().getId(), gameData,
                        graveyardOwnerId, null, null, entry.getXValue()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (legalTargets.isEmpty()) {
            return;
        }

        Collections.shuffle(legalTargets, ThreadLocalRandom.current());
        int battlefieldCount = Math.min(2, legalTargets.size());
        List<Card> battlefieldCards = new ArrayList<>(legalTargets.subList(0, battlefieldCount));
        List<Card> bottomCards = legalTargets.size() > 2
                ? new ArrayList<>(legalTargets.subList(2, legalTargets.size()))
                : List.of();

        if (!bottomCards.isEmpty()) {
            graveyardReturnSupport.processTargetedGraveyardTargets(
                    gameData, entry, bottomCards.stream().map(Card::getId).toList(),
                    (ignoredGraveyard, card) -> graveyardReturnSupport.moveCardToDestination(
                            gameData, graveyardOwnerId, card,
                            GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY,
                            effect.grantColor(), effect.grantSubtype(), effect.enterTapped()),
                    " puts ", " on the bottom of their library from graveyard.");
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : battlefieldCards) {
                if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                graveyardReturnSupport.applyPermanentGrants(permanent, effect.grantColor(), effect.grantSubtype());
                if (effect.enterTapped()) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                UUID battlefieldControllerId = effect.underOwnersControl()
                        ? graveyardOwnerId : entry.getControllerId();
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, battlefieldControllerId, permanent, enterTappedTypes, simultaneouslyEntered,
                        enteringCounters(effect));
                simultaneouslyEntered.add(permanent);
                entry.rememberReturnedPermanent(permanent.getId());
                applyReturnRiders(gameData, permanent, effect);
                returnedCards.add(card);
            }
            for (Permanent permanent : simultaneouslyEntered) {
                UUID battlefieldControllerId = effect.underOwnersControl()
                        ? graveyardOwnerId : entry.getControllerId();
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                        gameData, battlefieldControllerId, permanent, permanent.getCard());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!returnedCards.isEmpty()) {
            entry.setEventValue(returnedCards.size());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(graveyardOwnerId) + " returns " + returnedCards.size()
                            + " card(s) from the graveyard to the battlefield."));
        }
    }

    private Permanent sourceHost(GameData gameData, StackEntry entry) {
        Permanent snapshot = entry.getAttachedPermanentSnapshot();
        if (snapshot != null) {
            return gameQueryService.findPermanentById(gameData, snapshot.getId());
        }
        if (entry.getSourcePermanentId() == null) {
            return null;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null && source.isAttached()
                ? gameQueryService.findPermanentById(gameData, source.getAttachedTo())
                : null;
    }

    private EnterWithCountersEffect enteringCounters(ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        return effect.counterType() != null && effect.counterCount() > 0
                ? new EnterWithCountersEffect(effect.counterType(), new Fixed(effect.counterCount())) : null;
    }

    private record GraveyardCard(UUID ownerId, Card card) {
    }

    private void applyReturnRiders(GameData gameData, Permanent permanent,
                                   ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        if (effect.grantHaste()) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
        }
        if (effect.sacrificeAtEndStep()) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }
        if (effect.sacrificeAtEndStepIfManaValueAtLeast() > 0) {
            gameData.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast(
                    permanent.getId(), gameQueryService.findPermanentController(gameData, permanent.getId()),
                    effect.sacrificeAtEndStepIfManaValueAtLeast()));
        }
    }
}
