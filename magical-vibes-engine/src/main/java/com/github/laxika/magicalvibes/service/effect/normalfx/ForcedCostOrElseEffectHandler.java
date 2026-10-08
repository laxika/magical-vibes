package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromSingleGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCardFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ForcedCostOrElseEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final LibraryExileSupport libraryExileSupport;
    private final GraveyardTopExileSupport graveyardTopExileSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.DrawService drawService;
    private final com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService;
    private final GraveyardService graveyardService;
    private final LifeSupport lifeSupport;
    private final CoinFlipCostSupport coinFlipCostSupport;
    private AwardManaEffectHandler awardManaEffectHandler;

    @Autowired
    public ForcedCostOrElseEffectHandler(DestructionSupport destructionSupport,
            GameQueryService gameQueryService, PredicateEvaluationService predicateEvaluationService,
            PlayerInputService playerInputService, LibraryExileSupport libraryExileSupport,
            GraveyardTopExileSupport graveyardTopExileSupport, PermanentCounterSupport permanentCounterSupport,
            com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry,
            com.github.laxika.magicalvibes.service.DrawService drawService,
            com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService,
            GraveyardService graveyardService, LifeSupport lifeSupport,
            CoinFlipCostSupport coinFlipCostSupport) {
        this.destructionSupport = destructionSupport;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.playerInputService = playerInputService;
        this.libraryExileSupport = libraryExileSupport;
        this.graveyardTopExileSupport = graveyardTopExileSupport;
        this.permanentCounterSupport = permanentCounterSupport;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.drawService = drawService;
        this.amountEvaluationService = amountEvaluationService;
        this.graveyardService = graveyardService;
        this.lifeSupport = lifeSupport;
        this.coinFlipCostSupport = coinFlipCostSupport;
    }

    /** Compatibility constructor for focused handler tests that do not use life-gain costs. */
    public ForcedCostOrElseEffectHandler(DestructionSupport destructionSupport,
            GameQueryService gameQueryService, PredicateEvaluationService predicateEvaluationService,
            PlayerInputService playerInputService, LibraryExileSupport libraryExileSupport,
            GraveyardTopExileSupport graveyardTopExileSupport, PermanentCounterSupport permanentCounterSupport,
            com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry,
            com.github.laxika.magicalvibes.service.DrawService drawService,
            com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService,
            GraveyardService graveyardService) {
        this(destructionSupport, gameQueryService, predicateEvaluationService, playerInputService,
                libraryExileSupport, graveyardTopExileSupport, permanentCounterSupport,
                interactionHandlerRegistry, drawService, amountEvaluationService, graveyardService, null, null);
    }

    @Autowired
    void setAwardManaEffectHandler(AwardManaEffectHandler awardManaEffectHandler) {
        this.awardManaEffectHandler = awardManaEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForcedCostOrElseEffect.class;
    }

    public void resolvePaidEffects(GameData gameData, StackEntry entry, ForcedCostOrElseEffect effect,
            int xValue) {
        enqueuePaidEffects(gameData, entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), effect.paidEffects(), xValue, effect.forcedCost());
    }

    public void resolvePaidEffects(GameData gameData, PendingMayAbility ability, ForcedCostOrElseEffect effect,
            int xValue) {
        enqueuePaidEffects(gameData, ability.sourceCard(), ability.controllerId(), ability.sourcePermanentId(),
                ability.sourcePermanentSnapshot(), effect.paidEffects(), xValue, effect.forcedCost());
    }

    public void payFlipCoins(GameData gameData, UUID playerId, Card sourceCard, int count) {
        if (coinFlipCostSupport != null) {
            coinFlipCostSupport.pay(gameData, playerId, sourceCard, count);
        }
    }

    private void enqueuePaidEffects(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, Permanent sourcePermanentSnapshot, List<CardEffect> paidEffects, int xValue,
            CardEffect forcedCost) {
        if (paidEffects.isEmpty()) {
            return;
        }
        boolean cumulativeUpkeep = forcedCost instanceof PayManaCost manaCost && manaCost.forCumulativeUpkeep();
        if (cumulativeUpkeep && awardManaEffectHandler != null) {
            // "Cumulative upkeep — Add {R}" (Braid of Fire): adding the mana is the payment itself,
            // not a triggered ability, so it happens immediately instead of using the stack.
            StackEntry payment = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId,
                    sourceCard.getName() + "'s cumulative upkeep", new ArrayList<>(), xValue, sourcePermanentId);
            payment.setSourcePermanentSnapshot(sourcePermanentSnapshot);
            for (CardEffect effect : paidEffects) {
                if (effect instanceof AwardManaEffect) {
                    awardManaEffectHandler.resolve(gameData, payment, effect);
                }
            }
            paidEffects = paidEffects.stream()
                    .filter(effect -> !(effect instanceof AwardManaEffect))
                    .toList();
            if (paidEffects.isEmpty()) {
                return;
            }
        }
        List<CardEffect> snapshottedPaidEffects = paidEffects.stream()
                .map(effect -> snapshotPaidEffectXValue(effect, xValue))
                .toList();
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        int pendingIndex = gameData.pendingEffectResolutionIndex;
        if (pendingEntry != null
                && !cumulativeUpkeep
                && pendingEntry.getControllerId().equals(controllerId)
                && pendingEntry.getCard().getId().equals(sourceCard.getId())
                && pendingIndex >= 0
                && pendingIndex <= pendingEntry.getEffectsToResolve().size()) {
            pendingEntry.insertEffectsToResolve(pendingIndex, snapshottedPaidEffects);
            return;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId,
                sourceCard.getName() + "'s ability", new ArrayList<>(snapshottedPaidEffects), xValue, sourcePermanentId);
        entry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        gameData.enqueueTrigger(entry);
    }

    private CardEffect snapshotPaidEffectXValue(CardEffect effect, int xValue) {
        if (effect instanceof BoostSelfEffect boost) {
            return new BoostSelfEffect(
                    boost.powerBoost() instanceof XValue ? new Fixed(xValue) : boost.powerBoost(),
                    boost.toughnessBoost() instanceof XValue ? new Fixed(xValue) : boost.toughnessBoost());
        }
        return effect;
    }

    /**
     * Pays a mill-based forced cost when the supplied forced cost is one, returning empty for
     * other forced-cost types and false when the library can no longer pay it.
     */
    public Optional<Boolean> tryPayMillControllerCost(GameData gameData, ForcedCostOrElseEffect effect,
                                                      UUID playerId) {
        if (!(effect.forcedCost() instanceof MillControllerCost millCost)) {
            return Optional.empty();
        }
        var deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.size() < millCost.count()) {
            return Optional.of(false);
        }
        graveyardService.resolveMillPlayer(gameData, playerId, millCost.count());
        return Optional.of(true);
    }

    public boolean beginCumulativeUpkeepGraveyardPayment(GameData gameData, PendingMayAbility ability,
                                                         UUID controllerId) {
        ForcedCostOrElseEffect forcedCost = ability.effects().stream()
                .filter(ForcedCostOrElseEffect.class::isInstance)
                .map(ForcedCostOrElseEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (forcedCost == null
                || !(forcedCost.forcedCost() instanceof PutCardsFromSingleGraveyardOnBottomOfLibraryCost cost)
                || !hasEnoughCumulativeUpkeepCards(gameData, cost)) {
            return false;
        }

        gameData.graveyardTargetOperation.cumulativeUpkeepPayment =
                new GraveyardTargetOperationState.CumulativeUpkeepPaymentContext(
                        controllerId, ability.sourceCard(), ability.sourcePermanentId(), forcedCost,
                        cost.cardsPerPayment(), cost.payments(), List.of());
        gameData.graveyardTargetOperation.singleGraveyard = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId,
                allGraveyardCards(gameData), cost.cardsPerPayment(), cost.cardsPerPayment(),
                "Choose " + cost.cardsPerPayment()
                        + " cards from a single graveyard for the first cumulative upkeep payment.");
        return true;
    }

    public boolean beginControllerGraveyardPayment(GameData gameData, PendingMayAbility ability,
                                                    UUID controllerId) {
        ForcedCostOrElseEffect forcedCost = ability.effects().stream()
                .filter(ForcedCostOrElseEffect.class::isInstance)
                .map(ForcedCostOrElseEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (forcedCost == null
                || !(forcedCost.forcedCost() instanceof PutCardsFromGraveyardOnBottomOfLibraryCost cost)) {
            return false;
        }

        return beginControllerGraveyardPayment(gameData, ability.sourceCard(), ability.sourcePermanentId(),
                forcedCost, controllerId);
    }

    public boolean beginControllerGraveyardPayment(GameData gameData, Card sourceCard,
                                                    UUID sourcePermanentId,
                                                    ForcedCostOrElseEffect forcedCost,
                                                    UUID controllerId) {
        if (!(forcedCost.forcedCost() instanceof PutCardsFromGraveyardOnBottomOfLibraryCost cost)) {
            return false;
        }

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        if (graveyard.size() < cost.count()) {
            return false;
        }

        gameData.graveyardTargetOperation.controllerGraveyardPayment =
                new com.github.laxika.magicalvibes.model.GraveyardTargetOperationState
                        .ControllerGraveyardPaymentContext(
                            controllerId, sourceCard, sourcePermanentId, forcedCost,
                            cost.count());
        gameData.graveyardTargetOperation.singleGraveyard = false;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, graveyard,
                cost.count(), cost.count(), "Choose " + cost.count()
                        + " cards from your graveyard to put on the bottom of your library.");
        return true;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ForcedCostOrElseEffect) effect;

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost cost) {
            List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
            List<Card> candidates = graveyard == null ? List.of() : graveyard.stream()
                    .filter(card -> cost.requiredType() == null || card.hasType(cost.requiredType()))
                    .filter(card -> cost.predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null))
                    .toList();
            if (candidates.size() < cost.count()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (candidates.size() == cost.count()) {
                for (Card card : candidates) {
                    graveyard.remove(card);
                    gameData.addToExile(entry.getControllerId(), card);
                }
                return;
            }
            gameData.graveyardTargetOperation.card = entry.getCard();
            gameData.graveyardTargetOperation.controllerId = entry.getControllerId();
            gameData.graveyardTargetOperation.effects = List.of(e);
            gameData.graveyardTargetOperation.sourcePermanentId = entry.getSourcePermanentId();
            playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates,
                    cost.count(), cost.count(), "Choose " + cost.count() + " cards to exile from your graveyard.");
            return;
        }
        if (e.forcedCost() instanceof PayEchoCost echoCost) {
            String alternativeCost = gameQueryService.findAlternativeEchoCost(gameData, entry.getControllerId());
            CostEffect resolvedEchoCost;
            if (alternativeCost != null) {
                resolvedEchoCost = new com.github.laxika.magicalvibes.model.effect.PayManaCost(alternativeCost);
            } else if (echoCost.handCardCost() != null) {
                resolvedEchoCost = echoCost.handCardCost();
            } else if (echoCost.cost() != null) {
                resolvedEchoCost = echoCost.cost();
            } else {
                resolvedEchoCost = new com.github.laxika.magicalvibes.model.effect.PayManaCost(echoCost.echoCost());
            }
            e = new ForcedCostOrElseEffect(
                    resolvedEchoCost,
                    e.elseEffects(), e.optional(), e.anyPlayerMayPay(), e.payerIsEnchantedController(),
                    e.payerIsDefendingPlayer(), e.paidEffects());
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.FlipCoinsCost flipCost) {
            UUID payer = resolvePayer(gameData, entry, e);
            if (payer == null || coinFlipCostSupport == null) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payer, List.of(e),
                        entry.getCard().getName() + " - Flip " + flipCost.count() + " coin(s)?",
                        entry.getTargetId(), null, entry.getSourcePermanentId()));
                return;
            }
            coinFlipCostSupport.pay(gameData, payer, entry.getCard(), flipCost.count());
            return;
        }

        if (e.forcedCost() instanceof DiscardCardTypeCost discardCost) {
            UUID payer = resolvePayer(gameData, entry, e);
            List<Integer> validIndices = matchingHandIndices(gameData, payer, discardCost);
            if (payer == null || validIndices.size() < discardCost.count()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payer, List.of(e),
                        entry.getCard().getName() + " - Discard " + discardCost.count()
                                + " card(s) to pay cumulative upkeep?",
                        entry.getTargetId(), null, entry.getSourcePermanentId()));
                return;
            }
            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, payer, validIndices,
                    "Choose a card to discard for cumulative upkeep.", discardCost.count());
            return;
        }

        if (e.forcedCost() instanceof PayLifeCost payLifeCost) {
            UUID payer = resolvePayer(gameData, entry, e);
            if (payer == null) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            int lifeAmount = effectiveLifeAmount(gameData, payer, entry, payLifeCost);
            String prompt = entry.getCard().getName() + " - Pay " + lifeAmount + " life?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), payer, List.of(e), prompt,
                    entry.getTargetId(), null, entry.getSourcePermanentId()));
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayEnergyCost energyCost) {
            UUID payer = resolvePayer(gameData, entry, e);
            int energy = payer == null ? 0 : gameData.playerEnergyCounters.getOrDefault(payer, 0);
            if (payer == null || energy < energyCost.amount()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                String prompt = entry.getCard().getName() + " - Pay " + energyCost.amount()
                        + " energy counter(s)?";
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), payer, List.of(e), prompt,
                        entry.getTargetId(), null, entry.getSourcePermanentId(), null,
                        0, 0, null, null, null, entry.getSourcePermanentSnapshot(),
                        entry.getControllerId(), null));
                return;
            }
            gameData.setPlayerEnergyCounters(payer, energy - energyCost.amount());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayManaCost payCost) {
            // "You may pay {cost}; if you don't, [penalty]" — paying mana is always a choice, so
            // ask the controller unconditionally (the accept handler charges mana / checks canPay).
            // A dynamic reduction (Draco's Domain) is resolved now so the prompt and the accept
            // handler both use the already-reduced cost carried on the PendingMayAbility.
            String effectiveCost = payCost.manaCost();
            if (payCost.useSourceManaCost()) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                com.github.laxika.magicalvibes.model.Card sourceCard = source != null
                        ? source.getCard() : entry.getCard();
                effectiveCost = sourceCard.getManaCost();
                if (effectiveCost == null || effectiveCost.isEmpty()) {
                    // A permanent with no mana cost cannot pay its mana cost.
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }
            }
            if (payCost.genericReduction() != null || payCost.genericIncrease() != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                var context = com.github.laxika.magicalvibes.service.effect.AmountContext.forStackEntry(entry, source);
                int delta = payCost.genericReduction() == null ? 0
                        : amountEvaluationService.evaluate(gameData, payCost.genericReduction(), context);
                if (payCost.genericIncrease() != null) {
                    // "pay {1} for each other creature you control" (Fettergeist) — a negative
                    // reduction, so the same generic-portion adjustment covers both directions.
                    delta -= amountEvaluationService.evaluate(gameData, payCost.genericIncrease(), context);
                }
                effectiveCost = reduceGenericManaCost(payCost.manaCost(), delta);
            }
            // A blank mana cost means the payment is life-only (Glacial Chasm's "Pay 2 life"
            // cumulative upkeep), so the prompt drops the "{cost} and " part entirely.
            String lifePart = payCost.lifeAmount() > 0 ? payCost.lifeAmount() + " life" : "";
            String costText = effectiveCost.isEmpty()
                    ? lifePart
                    : effectiveCost + (lifePart.isEmpty() ? "" : " and " + lifePart);
            String prompt = entry.getCard().getName() + " - Pay " + costText + "?";
            if (e.anyPlayerMayPay()) {
                // "unless any player pays {cost}" (Icy Prison): offer each player in APNAP order;
                // first accept stops the sequence, full decline resolves the fallback.
                List<UUID> order = apnapOrder(gameData);
                UUID first = order.getFirst();
                gameData.forcedCostOrElseRemainingPlayers.clear();
                gameData.forcedCostOrElseRemainingPlayers.addAll(order.subList(1, order.size()));
                gameData.forcedCostOrElseSourceControllerId = entry.getControllerId();
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), first, List.of(e), prompt,
                        null, effectiveCost, entry.getSourcePermanentId()));
                return;
            }
            // "that player may pay" (Mind Whip): prompt the enchanted permanent's controller
            // carried on the stack entry's targetId, not the Aura's controller.
            UUID payer = resolvePayer(gameData, entry, e);
            if (payer == null) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getCard(), payer, List.of(e), prompt,
                    entry.getTargetId(), effectiveCost, entry.getSourcePermanentId(), null,
                    0, 0, null, null, null, entry.getSourcePermanentSnapshot(), entry.getControllerId(), null));
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost exileCost) {
            // "Cumulative upkeep — Exile the top card of your library" (Thought Lash): too few
            // cards means the cost can't be paid at all, so the penalty resolves immediately.
            if (!libraryExileSupport.hasAtLeast(gameData, entry.getControllerId(), exileCost.count())) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Exile the top " + exileCost.count()
                                + " card(s) of your library?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            libraryExileSupport.exileTopCards(gameData, entry.getControllerId(), exileCost.count());
            return;
        }

        if (e.forcedCost() instanceof PutCardsFromSingleGraveyardOnBottomOfLibraryCost cost) {
            if (!hasEnoughCumulativeUpkeepCards(gameData, cost)) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Put " + cost.cardsPerPayment()
                                + " card(s) from a single graveyard on the bottom of their owner's library "
                                + "for each of " + cost.payments() + " cumulative upkeep payment(s)?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            return;
        }

        if (e.forcedCost() instanceof OpponentGainsLifeCost lifeCost) {
            UUID sourceControllerId = entry.getControllerId();
            if (!canPayOpponentGainsLifeCost(gameData, sourceControllerId)) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), sourceControllerId, List.of(e),
                        entry.getCard().getName() + " - Have an opponent gain "
                                + lifeCost.amount() + " life?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            payOpponentGainsLifeCost(gameData, sourceControllerId, lifeCost, entry.getCard(), entry.getEntryType());
            return;
        }

        if (e.forcedCost() instanceof MillControllerCost millCost) {
            var deck = gameData.playerDecks.get(entry.getControllerId());
            if (deck == null || deck.size() < millCost.count()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Mill " + millCost.count() + " card(s)?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), millCost.count());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost graveyardCost) {
            // "Sacrifice this creature unless you exile the top creature card of your graveyard"
            // (Barrow Ghoul): no matching card means the cost can't be paid, so the penalty
            // resolves without a prompt.
            if (graveyardTopExileSupport.findTopMatching(gameData, entry.getControllerId(),
                    graveyardCost.requiredType()) == null) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Exile the top "
                                + graveyardExileLabel(graveyardCost.requiredType()) + "card of your graveyard?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            graveyardTopExileSupport.exileTopMatching(gameData, entry.getControllerId(),
                    graveyardCost.requiredType());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost returnFromGraveyardCost) {
            // "Sacrifice this creature unless you return a basic land card from your graveyard to
            // your hand" (Harvest Wurm): no matching card means the cost can't be paid, so the
            // penalty resolves without a prompt.
            if (!hasMatchingGraveyardCard(gameData, entry.getControllerId(), returnFromGraveyardCost.predicate())) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            String label = com.github.laxika.magicalvibes.model.filter.CardPredicateUtils
                    .describeFilter(returnFromGraveyardCost.predicate());
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Return a " + label + " from your graveyard to your hand?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            beginGraveyardReturnToHandChoice(gameData, entry.getControllerId(), returnFromGraveyardCost.predicate());
            return;
        }

        if (e.forcedCost() instanceof PutCardsFromGraveyardOnBottomOfLibraryCost cost) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of());
            if (graveyard.size() < cost.count()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Put " + cost.count()
                                + " cards from your graveyard on the bottom of your library?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            if (beginControllerGraveyardPayment(gameData, entry.getCard(), entry.getSourcePermanentId(),
                    e, entry.getControllerId())) {
                return;
            }
            destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            return;
        }

        if (e.forcedCost() instanceof PutCardFromGraveyardOnBottomOfLibraryCost) {
            List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
            if (graveyard == null || graveyard.isEmpty()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName()
                                + " - Put a card from your graveyard on the bottom of your library?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            beginGraveyardBottomChoice(gameData, entry.getControllerId());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost graveyardCost) {
            List<Integer> matchingIndices = graveyardTopExileSupport.matchingIndices(
                    gameData, entry.getControllerId(), graveyardCost);
            if (matchingIndices.isEmpty()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Exile a card from your graveyard?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, e.paidEffects());
            if (graveyardTopExileSupport.exileSoleMatching(gameData, entry.getControllerId(), graveyardCost)) {
                return;
            }
            interactionHandlerRegistry.begin(gameData, com.github.laxika.magicalvibes.model.PendingInteraction.GraveyardChoice
                    .builder(entry.getControllerId(), matchingIndices,
                            com.github.laxika.magicalvibes.model.GraveyardChoiceDestination.EXILE,
                            "Choose a card to exile from your graveyard.")
                    .mandatory(true)
                    .build());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost tokenCost) {
            // "Have an opponent create a … token" (Varchild's War-Riders): nothing can make this
            // unpayable, so the only question is whether the controller wants to pay.
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Have an opponent create "
                                + tokenCost.count() + " " + tokenCost.tokenTemplate().tokenName() + " token(s)?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            createOpponentTokens(gameData, entry.getControllerId(), tokenCost, entry.getCard().getName(),
                    entry.getCard().getSetCode());
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.DrawCardsCost drawCost) {
            // "Cumulative upkeep — Draw a card" (Psychic Vortex): drawing from an empty library is
            // still a legal payment, so this cost can never be unpayable.
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Draw " + drawCost.count() + " card(s)?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            for (int i = 0; i < drawCost.count(); i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost counterCost) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            UUID payerId = resolvePayer(gameData, entry, e);
            if (source == null || payerId == null
                    || !canPutCounterOnPermanent(gameData, source, counterCost.counterType())) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payerId, List.of(e),
                        entry.getCard().getName() + " - Put " + counterCost.count() + " "
                                + permanentCounterSupport.counterTypeName(counterCost.counterType())
                                + " counter(s) on it?",
                        entry.getTargetId(), null, entry.getSourcePermanentId(), null, 0, 0,
                        entry.getAttackedTargetId(), entry.getActivePlayerId(), payerId,
                        entry.getSourcePermanentSnapshot(), entry.getControllerId(), null));
                return;
            }
            if (payCounterOnSourceCost(gameData, entry, counterCost, payerId)) {
                resolvePaidEffects(gameData, entry, e, 0);
            }
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost counterCost) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null || !canPayCounterFromSource(source, counterCost)) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                String counterName = permanentCounterSupport.counterTypeName(counterCost.counterType());
                String counterText = counterCost.count() == 1
                        ? "a " + counterName + " counter"
                        : counterCost.count() + " " + counterName + " counters";
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Remove " + counterText + " from it?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            payCounterFromSourceCost(gameData, entry, counterCost);
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost) {
            // "sacrifice this unless you remove a counter from a permanent you control" (Chisei):
            // with no counters anywhere the cost cannot be paid, so the penalty resolves at once.
            List<UUID> candidates =
                    destructionSupport.collectPermanentIdsWithAnyCounter(gameData, entry.getControllerId());
            if (candidates.isEmpty()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Remove a counter from a permanent you control?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            removeCounterFromChosenPermanent(gameData, entry.getControllerId(), candidates, entry.getCard(),
                    entry.getSourcePermanentId(), e);
            return;
        }

        if (e.forcedCost() instanceof PutCounterOnControlledCreatureCost counterCost) {
            UUID payerId = resolvePayer(gameData, entry, e);
            List<UUID> candidates = payerId == null
                    ? List.of()
                    : destructionSupport.collectCreatureIds(gameData, payerId,
                            permanent -> canPutCounterOnPermanent(gameData, permanent, counterCost));
            if (candidates.isEmpty()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                String counterName = permanentCounterSupport.counterTypeName(counterCost.counterType());
                String counterText = counterCost.count() == 1
                        ? "a " + counterName + " counter"
                        : counterCost.count() + " " + counterName + " counters";
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payerId, List.of(e),
                        entry.getCard().getName() + " - Put " + counterText + " on a creature you control?",
                        entry.getTargetId(), null, entry.getSourcePermanentId(), null,
                        0, 0, null, null, null, entry.getSourcePermanentSnapshot(),
                        entry.getControllerId(), null));
                return;
            }
            if (!putCounterOnChosenPermanent(gameData, payerId, candidates, entry, counterCost)) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            }
            return;
        }

        if (e.forcedCost() instanceof PutCounterOnOpponentCreatureCost counterCost) {
            UUID payerId = resolvePayer(gameData, entry, e);
            UUID opponentId = payerId == null ? null : gameQueryService.getOpponentId(gameData, payerId);
            List<UUID> candidates = opponentId == null
                    ? List.of()
                    : destructionSupport.collectCreatureIds(gameData, opponentId,
                            permanent -> canPutCounterOnPermanent(gameData, permanent, counterCost.counterType()));
            if (candidates.isEmpty()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                String counterName = permanentCounterSupport.counterTypeName(counterCost.counterType());
                String counterText = counterCost.count() == 1
                        ? "a " + counterName + " counter"
                        : counterCost.count() + " " + counterName + " counters";
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payerId, List.of(e),
                        entry.getCard().getName() + " - Put " + counterText
                                + " on a creature an opponent controls?",
                        entry.getTargetId(), null, entry.getSourcePermanentId(), null,
                        0, 0, null, null, null, entry.getSourcePermanentSnapshot(),
                        entry.getControllerId(), null));
                return;
            }
            if (!putCounterOnChosenPermanent(gameData, payerId, candidates, entry, counterCost)) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            }
            return;
        }

        if (e.forcedCost() instanceof GainControlOfPermanentsCost gainCost) {
            UUID payerId = resolvePayer(gameData, entry, e);
            List<UUID> candidates = payerId == null
                    ? List.of()
                    : destructionSupport.collectPermanentIdsNotControlledBy(gameData, payerId, gainCost.filter());
            if (candidates.size() < gainCost.count()) {
                destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                return;
            }
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), payerId, List.of(e),
                        entry.getCard().getName() + " - Gain control of " + gainCost.count()
                                + " permanent(s)?", entry.getTargetId(), null, entry.getSourcePermanentId()));
                return;
            }
            beginGainControlPayment(gameData, payerId, candidates, entry.getCard(), entry.getSourcePermanentId(),
                    e, gainCost.count());
            return;
        }

        if (e.forcedCost() instanceof SacrificeMultiplePermanentsCost multiCost) {
            resolveMultiplePermanentSacrifice(gameData, entry, e, multiCost);
            return;
        }

        if (e.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost returnCost) {
            resolveMultiplePermanentReturnToHand(gameData, entry, e, returnCost);
            return;
        }

        if (!(e.forcedCost() instanceof SacrificePermanentCost sacrificePermanent)) {
                    log.warn("Game {} - Unsupported forced cost: {}", gameData.id, e.forcedCost().getClass().getSimpleName());
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                // payerIsEnchantedController: "that player may sacrifice …" — stack targetId is the
                // payer (enchanted controller / EACH_UPKEEP active player), not the source controller.
                UUID sourceControllerId = entry.getControllerId();
                UUID payerId = resolvePayer(gameData, entry, e);
                if (payerId == null) {
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }
                if (!gameQueryService.canEffectCauseSacrifice(gameData, payerId, sourceControllerId)) {
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }
                UUID sourcePermanentId = entry.getSourcePermanentId();
                FilterContext costFilterContext = FilterContext.of(gameData)
                        .withSourceCardId(entry.getCard().getId())
                        .withSourceControllerId(sourceControllerId)
                        .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

                List<UUID> matchingPermanentIds = destructionSupport.collectPermanentIds(gameData, payerId,
                        p -> (!sacrificePermanent.excludeSource() || !p.getId().equals(sourcePermanentId))
                                && gameQueryService.canSacrificePermanentForCosts(gameData, p)
                                && predicateEvaluationService.matchesPermanentPredicate(
                                p, sacrificePermanent.filter(), costFilterContext));

                if (matchingPermanentIds.isEmpty()) {
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                if (e.optional()) {
                    // "You may sacrifice ..." — ask the payer. Declining (handled in
                    // MayPenaltyChoiceHandlerService) resolves the fallback effects.
                    if (e.payerIsEnchantedController() || e.payerIsDefendingPlayer()) {
                        gameData.forcedCostOrElseSourceControllerId = sourceControllerId;
                    }
                    gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                            entry.getCard(), payerId, List.of(e),
                            entry.getCard().getName() + " - " + sacrificePermanent.description() + "?",
                            entry.getTargetId(), null, entry.getSourcePermanentId(),
                            null, 0, 0, entry.getAttackedTargetId(), null, null,
                            entry.getSourcePermanentSnapshot(), sourceControllerId, null));
                    return;
                }

                if (matchingPermanentIds.size() == 1) {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, matchingPermanentIds.getFirst());
                    if (permanent != null) {
                        destructionSupport.sacrificeAndLog(gameData, permanent, payerId);
                    } else {
                        destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    }
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ForcedCostOrElse(
                                payerId, entry.getSourcePermanentId(), entry.getCard(), e));
                playerInputService.beginPermanentChoice(gameData, payerId, matchingPermanentIds,
                        "Choose a permanent to sacrifice (" + sacrificePermanent.description() + ").");
                return;
    }

    private boolean hasEnoughCumulativeUpkeepCards(GameData gameData,
                                                    PutCardsFromSingleGraveyardOnBottomOfLibraryCost cost) {
        int availablePayments = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            availablePayments += graveyard.size() / cost.cardsPerPayment();
        }
        return availablePayments >= cost.payments();
    }

    private List<Card> allGraveyardCards(GameData gameData) {
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            cards.addAll(gameData.playerGraveyards.getOrDefault(playerId, List.of()));
        }
        return cards;
    }

    /**
     * The player who is asked to pay: the source's controller by default, the stack entry's
     * {@code targetId} for "that player may …" (Mind Whip / Pillar Tombs of Aku), or the defending
     * player of the attack that triggered the ability (Ogre Marauder).
     */
    private UUID resolvePayer(GameData gameData, StackEntry entry, ForcedCostOrElseEffect effect) {
        if (effect.payerIsEnchantedController()) {
            return entry.getTargetId();
        }
        if (effect.payerIsDefendingPlayer()) {
            UUID attackedTargetId = entry.getAttackedTargetId();
            if (attackedTargetId == null) {
                return null;
            }
            return gameData.playerIds.contains(attackedTargetId)
                    ? attackedTargetId
                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
        }
        return entry.getControllerId();
    }

    private List<Integer> matchingHandIndices(GameData gameData, UUID playerId,
            com.github.laxika.magicalvibes.model.effect.HandCardCost cost) {
        List<Integer> matchingIndices = new ArrayList<>();
        List<Card> hand = playerId == null ? null : gameData.playerHands.get(playerId);
        for (int i = 0; hand != null && i < hand.size(); i++) {
            Card card = hand.get(i);
            if (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null)) {
                matchingIndices.add(i);
            }
        }
        return matchingIndices;
    }

    private int effectiveLifeAmount(GameData gameData, UUID payer, StackEntry entry,
                                    PayLifeCost cost) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int sourceCounterCount = cost.perSourceCounter() == null || source == null
                ? 0
                : source.getCounterCount(cost.perSourceCounter());
        return cost.effectiveAmount(gameData.getLife(payer), sourceCounterCount);
    }

    /** Begins an exact counter payment for an optional forced cost, or completes it immediately. */
    public boolean beginControlledCounterPayment(GameData gameData, UUID payerId,
            PendingMayAbility ability, ForcedCostOrElseEffect effect) {
        RemoveCounterFromControlledPermanentCost cost =
                (RemoveCounterFromControlledPermanentCost) effect.forcedCost();
        List<Permanent> candidates = matchingCounterPermanents(gameData, payerId, ability, cost);
        if (totalMatchingCounters(candidates, cost) < cost.count()) {
            return false;
        }

        if (candidates.size() == 1 && availableCounterTypes(candidates.getFirst(), cost).size() == 1) {
            removeAllowedCounters(gameData, candidates.getFirst(), cost, cost.count());
            resolvePaidEffects(gameData, ability, effect, 0);
            return true;
        }

        beginControlledCounterChoice(gameData, payerId, ability, effect, cost, candidates);
        return true;
    }

    /** Completes one selection in an exact counter payment for an optional forced cost. */
    public boolean completeControlledCounterChoice(GameData gameData, String choice,
            ChoiceContext.RemoveCountersFromForcedCostOrElse context) {
        ChoiceContext.CounterSelection selection = context.permanentOptions().get(choice);
        if (selection == null) {
            throw new IllegalArgumentException("Invalid permanent choice: " + choice);
        }
        UUID permanentId = selection.permanentId();
        RemoveCounterFromControlledPermanentCost cost =
                (RemoveCounterFromControlledPermanentCost) context.effect().forcedCost();
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !context.payerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matchesCounterPayment(gameData, permanent, context.ability(), cost)
                || permanent.getCounterCount(selection.counterType()) < 1
                || !cost.allows(selection.counterType())) {
            throw new IllegalArgumentException("Permanent is no longer eligible for this counter payment");
        }

        gameData.interaction.clearAwaitingInput();
        permanentCounterSupport.removeCounterFromPermanent(
                gameData, permanent, selection.counterType(), 1);
        int remaining = context.remaining() - 1;
        if (remaining == 0) {
            resolvePaidEffects(gameData, context.ability(), context.effect(), 0);
            return true;
        }

        List<Permanent> candidates = matchingCounterPermanents(
                gameData, context.payerId(), context.ability(), cost);
        if (totalMatchingCounters(candidates, cost) < remaining) {
            throw new IllegalStateException("Counter payment became unaffordable");
        }
        beginControlledCounterChoice(gameData, context.payerId(), context.ability(), context.effect(), cost,
                candidates, remaining);
        return false;
    }

    private void beginControlledCounterChoice(GameData gameData, UUID payerId, PendingMayAbility ability,
            ForcedCostOrElseEffect effect, RemoveCounterFromControlledPermanentCost cost,
            List<Permanent> candidates) {
        beginControlledCounterChoice(gameData, payerId, ability, effect, cost, candidates, cost.count());
    }

    private void beginControlledCounterChoice(GameData gameData, UUID payerId, PendingMayAbility ability,
            ForcedCostOrElseEffect effect, RemoveCounterFromControlledPermanentCost cost,
            List<Permanent> candidates, int remaining) {
        playerInputService.beginRemoveCountersFromForcedCostOrElseChoice(
                gameData, ability, effect, payerId, remaining, counterPaymentOptions(candidates, cost));
    }

    private List<Permanent> matchingCounterPermanents(GameData gameData, UUID payerId,
            PendingMayAbility ability, RemoveCounterFromControlledPermanentCost cost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(payerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> matchesCounterPayment(gameData, permanent, ability, cost))
                .filter(permanent -> counterCount(permanent, cost) > 0)
                .toList();
    }

    private boolean matchesCounterPayment(GameData gameData, Permanent permanent, PendingMayAbility ability,
            RemoveCounterFromControlledPermanentCost cost) {
        return !(cost.excludeSource() && ability.sourcePermanentId() != null
                && ability.sourcePermanentId().equals(permanent.getId()))
                && (cost.permanentFilter() == null
                || predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, cost.permanentFilter()));
    }

    private int totalMatchingCounters(List<Permanent> candidates,
            RemoveCounterFromControlledPermanentCost cost) {
        return candidates.stream().mapToInt(permanent -> counterCount(permanent, cost)).sum();
    }

    private int counterCount(Permanent permanent, RemoveCounterFromControlledPermanentCost cost) {
        if (cost.allows(CounterType.ANY)) {
            return permanent.getCounters().entrySet().stream()
                    .filter(entry -> entry.getKey() != CounterType.ANY
                            && entry.getKey() != CounterType.SILVER)
                    .mapToInt(Map.Entry::getValue)
                    .sum();
        }
        return cost.allowedCounterTypes().stream()
                .filter(counterType -> counterType != CounterType.ANY && counterType != CounterType.SILVER)
                .mapToInt(permanent::getCounterCount)
                .sum();
    }

    private void removeAllowedCounters(GameData gameData, Permanent permanent,
            RemoveCounterFromControlledPermanentCost cost, int count) {
        for (int i = 0; i < count; i++) {
            removeAllowedCounter(gameData, permanent, cost);
        }
    }

    private void removeAllowedCounter(GameData gameData, Permanent permanent,
            RemoveCounterFromControlledPermanentCost cost) {
        for (CounterType counterType : cost.allowedCounterTypes()) {
            if (counterType == CounterType.ANY) {
                Optional<CounterType> presentCounter = permanent.getCounters().entrySet().stream()
                        .filter(entry -> entry.getKey() != CounterType.ANY
                                && entry.getKey() != CounterType.SILVER && entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .findFirst();
                if (presentCounter.isPresent()) {
                    permanentCounterSupport.removeCounterFromPermanent(gameData, permanent,
                            presentCounter.get(), 1);
                    return;
                }
            } else if (permanent.getCounterCount(counterType) > 0) {
                permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, counterType, 1);
                return;
            }
        }
        throw new IllegalStateException("Permanent has no counter matching this cost");
    }

    private List<CounterType> availableCounterTypes(Permanent permanent,
            RemoveCounterFromControlledPermanentCost cost) {
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(type -> type != CounterType.ANY && type != CounterType.SILVER && cost.allows(type))
                .sorted()
                .toList();
    }

    private Map<String, ChoiceContext.CounterSelection> counterPaymentOptions(
            List<Permanent> candidates, RemoveCounterFromControlledPermanentCost cost) {
        Map<String, Integer> totalsByName = new HashMap<>();
        for (Permanent candidate : candidates) {
            totalsByName.merge(candidate.getCard().getName(), 1, Integer::sum);
        }
        Map<String, Integer> occurrencesByName = new HashMap<>();
        Map<String, ChoiceContext.CounterSelection> options = new LinkedHashMap<>();
        for (Permanent candidate : candidates) {
            String name = candidate.getCard().getName();
            int occurrence = occurrencesByName.merge(name, 1, Integer::sum);
            String label = totalsByName.get(name) == 1 ? name : name + " (" + occurrence + ")";
            List<CounterType> types = availableCounterTypes(candidate, cost);
            for (CounterType type : types) {
                String option = types.size() == 1 ? label
                        : candidates.size() == 1
                        ? ChoiceContext.AdjustChosenCounterTypeChoice.counterLabel(type)
                        : label + " - " + ChoiceContext.AdjustChosenCounterTypeChoice.counterLabel(type);
                options.put(option, new ChoiceContext.CounterSelection(candidate.getId(), type));
            }
        }
        return options;
    }

    /**
     * Pays a {@link com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost}:
     * a single candidate sheds a counter immediately, several pause for a permanent choice. Shared
     * with the may-prompt accept path.
     */
    public void removeCounterFromChosenPermanent(GameData gameData, UUID payerId, List<UUID> candidates,
            com.github.laxika.magicalvibes.model.Card sourceCard, UUID sourcePermanentId,
            ForcedCostOrElseEffect effect) {
        if (candidates.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, candidates.getFirst());
            if (permanent != null) {
                destructionSupport.removeOneCounterAndLog(gameData, permanent, payerId);
            }
            return;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ForcedCostOrElse(payerId, sourcePermanentId, sourceCard, effect));
        playerInputService.beginPermanentChoice(gameData, payerId, candidates,
                "Choose a permanent to remove a counter from.");
    }

    /** Pays a creature-counter forced cost, pausing for a creature choice when needed. */
    public boolean putCounterOnChosenPermanent(GameData gameData, UUID payerId, List<UUID> candidates,
            StackEntry entry, PutCounterOnControlledCreatureCost cost) {
        return putCounterOnChosenPermanent(gameData, payerId, candidates, entry,
                cost.counterType(), cost.count());
    }

    /** Pays an opponent-creature counter forced cost, pausing for a creature choice when needed. */
    public boolean putCounterOnChosenPermanent(GameData gameData, UUID payerId, List<UUID> candidates,
            StackEntry entry, PutCounterOnOpponentCreatureCost cost) {
        if (candidates.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, candidates.getFirst());
            if (permanent == null || !canPutCounterOnPermanent(gameData, permanent, cost.counterType())) {
                return false;
            }
            StackEntry placementEntry = new StackEntry(entry);
            placementEntry.setControllerId(payerId);
            permanentCounterSupport.placeCounterOnPermanent(gameData, placementEntry, permanent,
                    cost.counterType(), cost.count());
            return true;
        }

        playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, 1,
                new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.OpponentCreatureCounterPlacement(
                        cost.counterType(), cost.count(), payerId),
                "Choose a creature to put a counter on.");
        return true;
    }

    private boolean putCounterOnChosenPermanent(GameData gameData, UUID payerId, List<UUID> candidates,
            StackEntry entry, com.github.laxika.magicalvibes.model.CounterType counterType, int count) {
        if (candidates.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, candidates.getFirst());
            if (permanent == null || !canPutCounterOnPermanent(gameData, permanent, counterType)) {
                return false;
            }
            StackEntry placementEntry = new StackEntry(entry);
            placementEntry.setControllerId(payerId);
            permanentCounterSupport.placeCounterOnPermanent(gameData, placementEntry, permanent,
                    counterType, count);
            return true;
        }

        playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, 1,
                new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.OwnPermanentCounterPlacementByPlayer(
                        counterType, count, payerId),
                "Choose a creature to put a counter on.");
        return true;
    }

    private boolean canPutCounterOnPermanent(GameData gameData, Permanent permanent,
            PutCounterOnControlledCreatureCost cost) {
        return canPutCounterOnPermanent(gameData, permanent, cost.counterType());
    }

    private boolean canPutCounterOnPermanent(GameData gameData, Permanent permanent,
            com.github.laxika.magicalvibes.model.CounterType counterType) {
        if (gameQueryService.cantHaveCounters(gameData, permanent)) {
            return false;
        }
        return switch (counterType) {
            case MINUS_ONE_MINUS_ONE -> !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent);
            case PLUS_ONE_PLUS_ONE -> !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent);
            default -> true;
        };
    }

    /**
     * Pays an {@link com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost}: the
     * payer's opponent creates one token per required count. Shared with the may-prompt accept path.
     */
    public void createOpponentTokens(GameData gameData, UUID payerId,
            com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost cost, String sourceName,
            String sourceSetCode) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
        if (opponentId == null) {
            return;
        }
        for (int i = 0; i < cost.count(); i++) {
            destructionSupport.createTokenForPlayer(gameData, opponentId, cost.tokenTemplate(), sourceName,
                    sourceSetCode);
        }
    }

    /** Whether the source controller's opponent can gain the life required by the cost. */
    public boolean canPayOpponentGainsLifeCost(GameData gameData, UUID sourceControllerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, sourceControllerId);
        return opponentId != null && gameQueryService.canPlayerGainLife(gameData, opponentId);
    }

    /** Pays an opponent-life-gain cost through the normal life-gain replacement and trigger path. */
    public boolean payOpponentGainsLifeCost(GameData gameData, UUID sourceControllerId,
            OpponentGainsLifeCost cost, Card sourceCard,
            com.github.laxika.magicalvibes.model.StackEntryType sourceEntryType) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, sourceControllerId);
        if (opponentId == null || !gameQueryService.canPlayerGainLife(gameData, opponentId)) {
            return false;
        }
        lifeSupport.applyGainLife(gameData, opponentId, cost.amount(), null, sourceCard, sourceEntryType);
        return true;
    }

    /** Pays a gain-control forced cost, prompting when more than one permanent is eligible. */
    public boolean beginGainControlPayment(GameData gameData, UUID payerId, List<UUID> candidates,
            com.github.laxika.magicalvibes.model.Card sourceCard, UUID sourcePermanentId,
            ForcedCostOrElseEffect effect, int count) {
        if (candidates.size() < count) {
            return false;
        }
        if (count == 1 && candidates.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, candidates.getFirst());
            if (target == null) {
                return false;
            }
            destructionSupport.gainPermanentControl(gameData, target, payerId, sourceCard.getName());
            return true;
        }
        ForcedCostOrElseEffect choiceEffect = effect;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ForcedCostOrElse(
                payerId, sourcePermanentId, sourceCard, choiceEffect));
        playerInputService.beginPermanentChoice(gameData, payerId, candidates,
                "Choose a permanent to gain control of.");
        return true;
    }

    /**
     * Pays a {@link com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost}: the
     * counters go on the source permanent itself (Aboroth's cumulative upkeep). Shared with the
     * may-prompt accept path. No-op if the source has already left the battlefield.
     */
    public void payCounterOnSourceCost(GameData gameData, StackEntry entry,
            com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost cost) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, cost.counterType(), cost.count());
    }

    /** Pays a legal source-counter cost for its actual payer, including replaced payments. */
    public boolean payCounterOnSourceCost(GameData gameData, StackEntry entry,
            com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost cost,
            UUID placingPlayerId) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !canPutCounterOnPermanent(gameData, source, cost.counterType())) {
            return false;
        }
        permanentCounterSupport.placeCounterOnPermanentForPlayer(
                gameData, entry, source, cost.counterType(), cost.count(), placingPlayerId);
        return true;
    }

    /** Pays a source-counter forced cost, returning false if the source or required counters are gone. */
    public boolean payCounterFromSourceCost(GameData gameData, StackEntry entry,
            com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost cost) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !canPayCounterFromSource(source, cost)) {
            return false;
        }

        permanentCounterSupport.removeCountersFromPermanent(gameData, source, cost.counterType(), cost.count());
        return true;
    }

    private boolean canPayCounterFromSource(Permanent source,
            com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost cost) {
        if (cost.counterType() == com.github.laxika.magicalvibes.model.CounterType.ANY) {
            return source.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.MINUS_ONE_MINUS_ONE)
                    + source.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)
                    >= cost.count();
        }
        return source.getCounterCount(cost.counterType()) >= cost.count();
    }

    /** Whether the player's graveyard holds at least one card matching {@code predicate}. */
    public boolean hasMatchingGraveyardCard(GameData gameData, UUID playerId,
            com.github.laxika.magicalvibes.model.filter.CardPredicate predicate) {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = gameData.playerGraveyards.get(playerId);
        return graveyard != null && graveyard.stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(card, predicate, null));
    }

    /**
     * Pays a {@link com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost}:
     * the payer picks one matching graveyard card, which goes to its owner's hand. Mandatory — the
     * payer already committed to the cost by accepting the may prompt. Shared with that accept path.
     */
    public void beginGraveyardReturnToHandChoice(GameData gameData, UUID playerId,
            com.github.laxika.magicalvibes.model.filter.CardPredicate predicate) {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; graveyard != null && i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), predicate, null)) {
                matchingIndices.add(i);
            }
        }
        String label = com.github.laxika.magicalvibes.model.filter.CardPredicateUtils.describeFilter(predicate);
        interactionHandlerRegistry.begin(gameData,
                com.github.laxika.magicalvibes.model.PendingInteraction.GraveyardChoice
                        .builder(playerId, matchingIndices,
                                com.github.laxika.magicalvibes.model.GraveyardChoiceDestination.HAND,
                                "Choose a " + label + " to return to your hand.")
                        .mandatory(true)
                        .build());
    }

    /** Pays by putting one card from the controller's graveyard on the bottom of its library. */
    public void beginGraveyardBottomChoice(GameData gameData, UUID playerId) {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> cardIndices = new ArrayList<>();
        for (int i = 0; graveyard != null && i < graveyard.size(); i++) {
            cardIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData,
                com.github.laxika.magicalvibes.model.PendingInteraction.GraveyardChoice
                        .builder(playerId, cardIndices,
                                com.github.laxika.magicalvibes.model.GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY,
                                "Choose a card to put on the bottom of your library.")
                        .mandatory(true)
                        .build());
    }

    /** "creature " / "" — the type words in an exile-from-graveyard prompt. */
    private String graveyardExileLabel(com.github.laxika.magicalvibes.model.CardType requiredType) {
        return requiredType == null ? "" : requiredType.name().toLowerCase() + " ";
    }

    /** Seating order rotated so the active player is first (APNAP). */
    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    /**
     * Subtracts {@code reduction} from the generic portion of a mana cost string (floored at 0),
     * preserving any colored symbols — "this cost is reduced by {N}" (Draco). A negative
     * {@code reduction} raises the generic portion instead ("pay {1} for each …", Fettergeist).
     */
    private String reduceGenericManaCost(String costString, int reduction) {
        com.github.laxika.magicalvibes.model.ManaCost cost =
                new com.github.laxika.magicalvibes.model.ManaCost(costString);
        int generic = Math.max(0, cost.getGenericCost() - reduction);
        StringBuilder sb = new StringBuilder();
        var colored = cost.getColoredCosts();
        if (generic > 0 || colored.isEmpty()) {
            sb.append("{").append(generic).append("}");
        }
        colored.forEach((color, count) -> {
            for (int i = 0; i < count; i++) {
                sb.append("{").append(color.getCode()).append("}");
            }
        });
        return sb.toString();
    }

    /**
     * "Sacrifice [source] unless you sacrifice N matching permanents" (e.g. Rathi Dragon). If the
     * controller has fewer than N matching permanents the cost cannot be paid and the fallback
     * effects resolve. When {@code optional} the controller is asked ("you may"); otherwise the
     * sacrifice is forced (choosing which when they control more than N).
     */
    private void resolveMultiplePermanentSacrifice(GameData gameData, StackEntry entry,
            ForcedCostOrElseEffect e, SacrificeMultiplePermanentsCost multiCost) {
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, multiCost.filter()));

        if (matchingIds.size() < multiCost.count()) {
            destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            return;
        }

        if (e.optional()) {
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getCard(), controllerId, List.of(e),
                    entry.getCard().getName() + " - Sacrifice " + multiCost.count() + " permanents?",
                    null, null, entry.getSourcePermanentId()));
            return;
        }

        destructionSupport.sacrificePlayerMatchingPermanents(gameData, controllerId, multiCost.count(), multiCost.filter());
    }

    /**
     * "Sacrifice [source] unless you return N matching permanents to their owner's hand"
     * (e.g. Ovinomancer). Same may/can't-pay shape as {@link #resolveMultiplePermanentSacrifice}.
     */
    private void resolveMultiplePermanentReturnToHand(GameData gameData, StackEntry entry,
            ForcedCostOrElseEffect e,
            com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost returnCost) {
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, returnCost.filter()));

        if (matchingIds.size() < returnCost.count()) {
            destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
            return;
        }

        if (e.optional()) {
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getCard(), controllerId, List.of(e),
                    entry.getCard().getName() + " - Return " + returnCost.count()
                            + " permanent(s) to their owner's hand?",
                    null, null, entry.getSourcePermanentId()));
            return;
        }

        destructionSupport.returnPlayerMatchingPermanents(gameData, controllerId, returnCost.count(), returnCost.filter());
    }
}
