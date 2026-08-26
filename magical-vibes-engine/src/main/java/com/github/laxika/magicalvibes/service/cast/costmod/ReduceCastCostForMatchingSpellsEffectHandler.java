package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForMatchingSpellsEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForMatchingSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceCastCostForMatchingSpellsEffect) effect;
        boolean applies = switch (reduce.scope()) {
            case SELF -> source.controlledBy(context.castingPlayerId());
            case OPPONENT -> !source.controlledBy(context.castingPlayerId());
            case ALL -> true;
        };
        if (!applies) {
            return 0;
        }
        if (reduce.plotFromHandOnly() != context.plottingFromHand()) {
            return 0;
        }
        if (reduce.faceDownOnly() && !context.castFaceDown()) {
            return 0;
        }
        if (!reduce.sourceZones().isEmpty()
                && (context.sourceZone() == null
                ? reduce.sourceZones().stream().noneMatch(zone -> spellWasCastFromZone(
                        context.gameData(), context.spell(), zone))
                : !reduce.sourceZones().contains(context.sourceZone()))) {
            return 0;
        }
        if (!predicateEvaluationService.matchesCardPredicate(
                context.spell(), reduce.predicate(),
                source.sourcePermanent() == null ? null : source.sourcePermanent().getCard().getId(),
                context.gameData(), context.castingPlayerId())) {
            return 0;
        }
        // Evaluated against the source permanent so source-relative amounts (counters on this
        // creature) work; the spell being cast has no permanent of its own yet.
        var amountContext = new AmountContext(context.castingPlayerId(), source.sourcePermanent(),
                null, 0, 0);
        return -amountEvaluationService.evaluate(context.gameData(), reduce.amount(), amountContext);
    }

    private boolean spellWasCastFromZone(GameData gameData, Card spell, Zone zone) {
        return switch (zone) {
            case EXILE -> gameData.findExiledCard(spell.getId()) != null;
            case GRAVEYARD -> gameData.playerGraveyards.values().stream()
                    .anyMatch(graveyard -> graveyard.stream()
                            .anyMatch(card -> card.getId().equals(spell.getId())));
            case HAND -> gameData.playerHands.values().stream()
                    .anyMatch(hand -> hand.stream().anyMatch(card -> card.getId().equals(spell.getId())));
            default -> false;
        };
    }
}
