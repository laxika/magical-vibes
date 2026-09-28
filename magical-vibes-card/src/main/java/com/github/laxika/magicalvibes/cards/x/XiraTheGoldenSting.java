package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterAndWatchTargetCreatureDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "48")
@CardRegistration(set = "DMC", collectorNumber = "70")
public class XiraTheGoldenSting extends Card {

    public XiraTheGoldenSting() {
        PermanentPredicate anotherCreatureWithoutEggCounter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.EGG))));

        CreateTokenEffect insect = new CreateTokenEffect(
                "Insect", 1, 1, CardColor.BLACK, List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING), Set.of());
        target(new PermanentPredicateTargetFilter(
                anotherCreatureWithoutEggCounter,
                "Target must be another creature without an egg counter on it"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new PutCounterAndWatchTargetCreatureDeathEffect(
                                CounterType.EGG,
                                anotherCreatureWithoutEggCounter,
                                SequenceEffect.of(new DrawCardEffect(), insect)));
    }
}
