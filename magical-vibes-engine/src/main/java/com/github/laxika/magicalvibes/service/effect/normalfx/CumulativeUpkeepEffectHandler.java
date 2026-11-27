package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromSingleGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cumulative upkeep (CR 702.24a): "if this permanent is on the battlefield, put an age counter on this permanent. Then
 * you may pay [cost] for each age counter on it. If you don't, sacrifice it." The age counter is put, the counters are
 * read then (CR 702.24b: every age counter on the permanent, whichever instance put it), and the payment is the next
 * step of the same resolution: an {@link UnlessPaysEffect} whose ransom is the cost once for each age counter, paid in
 * full or not at all (CR 118.3), each choice the cost names made once for each counter, with "sacrifice it" unless
 * it is paid (CR 118.12a).
 *
 * <p>"Whenever this permanent's cumulative upkeep is paid" and "when a player doesn't pay this permanent's cumulative
 * upkeep" (Balduvian Fallen, Hibernation's End; Heart of Bogardan, Thought Lash) are triggered abilities the choice
 * triggers, put on the stack after this one resolves ({@link QueueReflexiveAbilityEffect}, CR 603.2, 603.12). Braid of
 * Fire's "Add {R}" is the payment itself, so its mana is added as it is paid.
 */
@Component
@RequiredArgsConstructor
public class CumulativeUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
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

        List<CardEffect> unpaid = new ArrayList<>();
        unpaid.add(new SacrificeSelfEffect());
        for (CardEffect unpaidEffect : snapshotAgeRelativeAmounts(gameData, entry, self, e.unpaidEffects())) {
            unpaid.add(unpaidEffect instanceof QueueReflexiveAbilityEffect
                    ? unpaidEffect : new QueueReflexiveAbilityEffect(unpaidEffect));
        }
        List<CardEffect> paid = new ArrayList<>();
        for (CardEffect paidEffect : e.paidEffects()) {
            paid.add(paidEffect instanceof AwardManaEffect || paidEffect instanceof QueueReflexiveAbilityEffect
                    ? paidEffect : new QueueReflexiveAbilityEffect(paidEffect));
        }

        UnlessPaysEffect payOrSacrifice = new UnlessPaysEffect(Subjects.you(), ransom(e, ageCounters),
                sequence(unpaid), paid.isEmpty() ? null : sequence(paid), prompt(e, ageCounters));
        entry.spliceSequenceSteps(entry.getResolvingEffectIndex(), List.of(payOrSacrifice), false);
    }

    /** The cost of {@code e} once for each of {@code ageCounters} age counters (CR 702.24a). */
    private static CostEffect ransom(CumulativeUpkeepEffect e, int ageCounters) {
        if (e.flipCoinPerAge()) {
            return new FlipCoinsCost(ageCounters);
        }
        if (e.isSacrificeCost()) {
            // Different permanents for each age counter, sacrificed together (CR 702.24a's example).
            return new SacrificeMultiplePermanentsCost(ageCounters, e.sacrificeFilter(),
                    ageCounters == 1 ? e.sacrificeNoun() : e.sacrificeNoun() + "s");
        }
        if (e.opponentTokenPerAge() != null) {
            return new OpponentCreatesTokensCost(ageCounters, e.opponentTokenPerAge());
        }
        if (e.counterTypePerAge() != null) {
            return new PutTypedCounterOnSourceCost(e.counterTypePerAge(), ageCounters);
        }
        if (e.opponentCreatureCounterTypePerAge() != null) {
            return new PutCounterOnOpponentCreatureCost(e.opponentCreatureCounterTypePerAge(), ageCounters);
        }
        if (e.drawCardsPerAge()) {
            return new DrawCardsCost(ageCounters);
        }
        if (e.discardCardsPerAge()) {
            return new DiscardCardTypeCost(null, null, ageCounters);
        }
        if (e.exileTopCardsPerAge()) {
            return new ExileTopCardOfLibraryCost(ageCounters);
        }
        if (e.putCardsFromSingleGraveyardPerAge() > 0) {
            return new PutCardsFromSingleGraveyardOnBottomOfLibraryCost(e.putCardsFromSingleGraveyardPerAge(),
                    ageCounters);
        }
        if (e.opponentLifeGainPerAge() > 0) {
            return new OpponentGainsLifeCost(e.opponentLifeGainPerAge() * ageCounters, Math.max(1, ageCounters));
        }
        if (e.gainControlFilter() != null) {
            return new GainControlOfPermanentsCost(ageCounters, e.gainControlFilter());
        }
        int life = e.lifePerAge() * ageCounters;
        if (e.costPerAge().isEmpty()) {
            return new PayLifeCost(life);
        }
        return new PayManaCost(manaFor(e.costPerAge(), ageCounters), null, true, life);
    }

    /**
     * {@code costPerAge} paid {@code times} times: a generic cost summed ("pay {4}", the cumulative upkeep rulings'
     * wording), any other cost repeated, each symbol paid on its own ({G/W} once for each counter, CR 702.24a).
     */
    private static String manaFor(String costPerAge, int times) {
        ManaCost perAge = new ManaCost(costPerAge);
        if (costPerAge.equals("{" + perAge.getGenericCost() + "}")) {
            return "{" + perAge.getGenericCost() * times + "}";
        }
        return costPerAge.repeat(times);
    }

    /**
     * The question asked when the payment is mana that adds mana instead (Braid of Fire's "Add {R}"): what paying it
     * does; else {@code null}, the question the ransom reads ("Pay {2}?").
     */
    private String prompt(CumulativeUpkeepEffect e, int ageCounters) {
        if (!"{0}".equals(e.costPerAge())) {
            return null;
        }
        return e.paidEffects().stream()
                .filter(AwardManaEffect.class::isInstance)
                .map(AwardManaEffect.class::cast)
                .findFirst()
                .map(mana -> "Add " + ("{" + mana.color().getCode() + "}").repeat(ageCounters) + "?")
                .orElse(null);
    }

    private static CardEffect sequence(List<CardEffect> steps) {
        return steps.size() == 1 ? steps.getFirst() : new SequenceEffect(steps);
    }

    /**
     * Freezes source-relative amounts in the unpaid-branch effects into constants while the source is still on the
     * battlefield. The sacrifice runs first on that branch, so by the time the companion "when a player doesn't pay
     * …" ability is made the age counters are gone: Heart of Bogardan's "X is twice the number of age counters on this
     * enchantment minus 2" is read as the payment is asked (CR 608.2h).
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
