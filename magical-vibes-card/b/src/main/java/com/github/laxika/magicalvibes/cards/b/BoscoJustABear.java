package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "132")
public class BoscoJustABear extends Card {

    public BoscoJustABear() {
        PermanentCount legendaryCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateTokenEffect.ofArtifactToken(legendaryCreaturesYouControl, "Food", List.of(CardSubtype.FOOD), List.of(
                        new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this token: You gain 3 life."))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                "Sacrifice a Food"),
                        new PutCountersOnSourceEffect(1, 1, 2),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{2}{G}, Sacrifice a Food: Put two +1/+1 counters on Bosco. He gains trample until end of turn."));
    }
}
