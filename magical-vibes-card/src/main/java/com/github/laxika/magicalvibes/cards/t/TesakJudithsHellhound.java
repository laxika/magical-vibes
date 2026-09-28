package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MKC", collectorNumber = "36")
@CardRegistration(set = "MKC", collectorNumber = "346")
public class TesakJudithsHellhound extends Card {

    public TesakJudithsHellhound() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, cantBlockUnlessHasNoPlusOnePlusOneCounter());

        PermanentHasSubtypePredicate dogs = new PermanentHasSubtypePredicate(CardSubtype.DOG);
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new UnleashEffect(), GrantScope.OWN_CREATURES, dogs));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                cantBlockUnlessHasNoPlusOnePlusOneCounter(), GrantScope.OWN_CREATURES, dogs));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.ANY)));

        addEffect(EffectSlot.ON_ATTACK, new AwardManaEffect(
                ManaColor.RED,
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER)));
    }

    private CantBlockUnlessEffect cantBlockUnlessHasNoPlusOnePlusOneCounter() {
        return new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it");
    }
}
