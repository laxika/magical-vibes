package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForcedCostOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ForcedCostOrElseEffect) effect;

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
            // "Cumulative upkeep — Put a -1/-1 counter on this creature" (Aboroth): the payment only
            // touches the source, so it can never be unpayable — the controller just chooses.
            if (e.optional()) {
                gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                        entry.getCard(), entry.getControllerId(), List.of(e),
                        entry.getCard().getName() + " - Put " + counterCost.count() + " "
                                + permanentCounterSupport.counterTypeName(counterCost.counterType())
                                + " counter(s) on it?",
                        null, null, entry.getSourcePermanentId()));
                return;
            }
            payCounterOnSourceCost(gameData, entry, counterCost);
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
                UUID sourcePermanentId = entry.getSourcePermanentId();

                List<UUID> matchingPermanentIds = destructionSupport.collectPermanentIds(gameData, payerId,
                        p -> (!sacrificePermanent.excludeSource() || !p.getId().equals(sourcePermanentId))
                                && predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacrificePermanent.filter()));

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
                            entry.getTargetId(), null, entry.getSourcePermanentId()));
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
