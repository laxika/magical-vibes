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
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getCard(), entry.getControllerId(), List.of(e),
                    entry.getCard().getName() + " - Pay " + payCost.manaCost() + "?",
                    null, payCost.manaCost(), entry.getSourcePermanentId()));
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

                List<UUID> matchingPermanentIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                        p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacrificePermanent.filter()));

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
