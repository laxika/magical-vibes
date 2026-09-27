package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "2")
@CardRegistration(set = "C21", collectorNumber = "329")
public class FelisaFangOfSilverquill extends Card {

    public FelisaFangOfSilverquill() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentPowerLessThanSourcePowerPredicate())),
                "Target must be an attacking creature with lesser power"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        CreateTokenEffect inkling = new CreateTokenEffect(
                1, "Inkling", 2, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.INKLING), Set.of(Keyword.FLYING), Set.of())
                .withTapped(true);
        CreateTokensForEachDyingSourceCounterEffect createInklings =
                new CreateTokensForEachDyingSourceCounterEffect(inkling);
        TriggeringPermanentConditionalEffect nontokenWithCounters =
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                new PermanentHasCountersPredicate(CounterType.ANY))),
                        createInklings);

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, nontokenWithCounters);
        addEffect(EffectSlot.ON_DEATH, createInklings);
    }
}
