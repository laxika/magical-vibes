package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "148")
public class WingedHiveTyrant extends Card {

    public WingedHiveTyrant() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.HASTE),
                GrantScope.OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.ANY)));
    }
}
