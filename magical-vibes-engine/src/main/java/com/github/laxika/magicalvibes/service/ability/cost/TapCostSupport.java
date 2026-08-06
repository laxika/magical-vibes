package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Evaluates how many permanents a {@link TapMultiplePermanentsCost} demands.
 *
 * <p>The cost carries a {@code DynamicAmount}, but {@link PermanentChoiceCostHandler#requiredCount()}
 * has no game state to evaluate it against, so the count is resolved here — once, wherever the
 * handler is built — and handed to the handler as a plain int. The only amounts in use are
 * {@code Fixed} (every printed "tap N untapped …" cost) and {@code XValue} (Aryel, Knight of
 * Windgrace's "tap X untapped Knights you control"), neither of which reads the controller or the
 * source permanent; both are supplied anyway so a counting amount would evaluate correctly.
 */
@Component
@RequiredArgsConstructor
public class TapCostSupport {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    /**
     * @param sourcePermanentId the permanent whose ability carries the cost, or {@code null} for a
     *                          cost paid outside the battlefield (a graveyard-activated ability)
     * @param xValue            the X announced at activation, read by an {@code XValue} count
     */
    public int requiredCount(GameData gameData, TapMultiplePermanentsCost cost, UUID sourcePermanentId, int xValue) {
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        UUID controllerId = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentController(gameData, sourcePermanentId);
        return amountEvaluationService.evaluate(gameData, cost.count(),
                new AmountContext(controllerId, source, null, xValue, 0));
    }
}
