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
            UUID payer = e.payerIsEnchantedController() ? entry.getTargetId() : entry.getControllerId();
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getCard(), payer, List.of(e), prompt,
                    entry.getTargetId(), effectiveCost, entry.getSourcePermanentId()));
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
            createOpponentTokens(gameData, entry.getControllerId(), tokenCost, entry.getCard().getName());
            return;
        }

        if (e.forcedCost() instanceof SacrificeMultiplePermanentsCost multiCost) {
            resolveMultiplePermanentSacrifice(gameData, entry, e, multiCost);
            return;
        }

        if (!(e.forcedCost() instanceof SacrificePermanentCost sacrificePermanent)) {
                    log.warn("Game {} - Unsupported forced cost: {}", gameData.id, e.forcedCost().getClass().getSimpleName());
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                UUID controllerId = entry.getControllerId();
                UUID sourcePermanentId = entry.getSourcePermanentId();

                List<UUID> matchingPermanentIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                        p -> (!sacrificePermanent.excludeSource() || !p.getId().equals(sourcePermanentId))
                                && predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacrificePermanent.filter()));

                if (matchingPermanentIds.isEmpty()) {
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                if (e.optional()) {
                    // "You may sacrifice ..." — ask the controller. Declining (handled in
                    // MayPenaltyChoiceHandlerService) resolves the fallback effects.
                    gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                            entry.getCard(), controllerId, List.of(e),
                            entry.getCard().getName() + " - " + sacrificePermanent.description() + "?",
                            null, null, entry.getSourcePermanentId()));
                    return;
                }

                if (matchingPermanentIds.size() == 1) {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, matchingPermanentIds.getFirst());
                    if (permanent != null) {
                        destructionSupport.sacrificeAndLog(gameData, permanent, controllerId);
                    } else {
                        destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    }
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ForcedCostOrElse(
                                controllerId, entry.getSourcePermanentId(), entry.getCard(), e));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingPermanentIds,
                        "Choose a permanent to sacrifice (" + sacrificePermanent.description() + ").");
    }

    /**
     * Pays an {@link com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost}: the
     * payer's opponent creates one token per required count. Shared with the may-prompt accept path.
     */
    public void createOpponentTokens(GameData gameData, UUID payerId,
            com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost cost, String sourceName) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
        if (opponentId == null) {
            return;
        }
        for (int i = 0; i < cost.count(); i++) {
            destructionSupport.createTokenForPlayer(gameData, opponentId, cost.tokenTemplate(), sourceName, null);
        }
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
}
