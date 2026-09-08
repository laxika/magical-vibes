package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "199")
public class SpineseekerCentipede extends Card {

    public SpineseekerCentipede() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(CardPredicateUtils.basicLand()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(),
                new StaticBoostEffect(1, 2, Set.of(Keyword.VIGILANCE), GrantScope.SELF)));
    }
}
