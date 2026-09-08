package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromSingleGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cumulative upkeep (CR 702.24): put an age counter, then you may pay the cost for each age counter
 * or sacrifice the permanent. Mana payment is flagged for cumulative-upkeep-only mana; sacrifice
 * costs reuse {@link SacrificeMultiplePermanentsCost} (one matching permanent per age counter).
 */
@Component
@RequiredArgsConstructor
public class CumulativeUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CumulativeUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CumulativeUpkeepEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, CounterType.AGE, 1);
        int ageCounters = self.getCounterCount(CounterType.AGE);
        if (ageCounters <= 0) {
            return;
        }

        // "When a player doesn't pay this permanent's cumulative upkeep, …" (Thought Lash) rides
        // along with the sacrifice on the unpaid branch.
        List<CardEffect> unpaid = new ArrayList<>();
        unpaid.add(new SacrificeSelfEffect());
        unpaid.addAll(snapshotAgeRelativeAmounts(gameData, entry, self, e.unpaidEffects()));

        ForcedCostOrElseEffect payOrSacrifice;
        if (e.flipCoinPerAge()) {
            payOrSacrifice = new ForcedCostOrElseEffect(new FlipCoinsCost(ageCounters), unpaid, true);
        } else if (e.isSacrificeCost()) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new SacrificeMultiplePermanentsCost(ageCounters, e.sacrificeFilter()), unpaid, true);
        } else if (e.opponentTokenPerAge() != null) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new OpponentCreatesTokensCost(ageCounters, e.opponentTokenPerAge()), unpaid, true);
        } else if (e.counterTypePerAge() != null) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new PutTypedCounterOnSourceCost(e.counterTypePerAge(), ageCounters), unpaid, true);
        } else if (e.opponentCreatureCounterTypePerAge() != null) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new PutCounterOnOpponentCreatureCost(e.opponentCreatureCounterTypePerAge(), ageCounters),
                    unpaid, true);
        } else if (e.drawCardsPerAge()) {
            payOrSacrifice = new ForcedCostOrElseEffect(new DrawCardsCost(ageCounters), unpaid, true);
        } else if (e.discardCardsPerAge()) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new DiscardCardTypeCost(null, null, ageCounters), unpaid, true);
        } else if (e.exileTopCardsPerAge()) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new ExileTopCardOfLibraryCost(ageCounters), unpaid, true);
        } else if (e.putCardsFromSingleGraveyardPerAge() > 0) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new PutCardsFromSingleGraveyardOnBottomOfLibraryCost(
                            e.putCardsFromSingleGraveyardPerAge(), ageCounters), unpaid, true);
        } else if (e.opponentLifeGainPerAge() > 0) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new OpponentGainsLifeCost(e.opponentLifeGainPerAge() * ageCounters), unpaid, true);
        } else if (e.gainControlFilter() != null) {
            payOrSacrifice = new ForcedCostOrElseEffect(
                    new GainControlOfPermanentsCost(ageCounters, e.gainControlFilter()), unpaid, true);
        } else {
            String totalCost = e.costPerAge().repeat(ageCounters);
            int totalLife = e.lifePerAge() * ageCounters;
            payOrSacrifice = e.paidEffects().isEmpty()
                    ? new ForcedCostOrElseEffect(new PayManaCost(totalCost, null, true, totalLife), unpaid, true)
                    : new ForcedCostOrElseEffect(new PayManaCost(totalCost, null, true, totalLife), unpaid, true,
                            e.paidEffects());
        }
        forcedCostOrElseEffectHandler.resolve(gameData, entry, payOrSacrifice);
    }

    /**
     * Freezes source-relative amounts in the unpaid-branch effects into constants while the source
     * is still on the battlefield. The sacrifice runs first on that branch, so by the time the
     * companion "when a player doesn't pay …" effect resolves the age counters are gone —
     * Heart of Bogardan's "X is twice the number of age counters on this enchantment minus 2" has
     * to be read from last-known information (CR 608.2h).
     */
    private List<CardEffect> snapshotAgeRelativeAmounts(GameData gameData, StackEntry entry, Permanent self,
            List<CardEffect> effects) {
        AmountContext context = AmountContext.forStackEntry(entry, self);
        return effects.stream()
                .map(fx -> fx instanceof DealDamageToTargetAndTheirCreaturesEffect damage
                        ? new DealDamageToTargetAndTheirCreaturesEffect(new Fixed(Math.max(0,
                                amountEvaluationService.evaluate(gameData, damage.amount(), context))))
                        : fx)
                .toList();
    }
}
