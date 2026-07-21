package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "136")
public class TenaciousHunter extends Card {

    public TenaciousHunter() {
        // As long as a creature has a -1/-1 counter on it, this creature has vigilance and deathtouch.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE)))),
                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.DEATHTOUCH), GrantScope.SELF)));
    }
}
