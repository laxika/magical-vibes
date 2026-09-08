package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "226")
public class Warpath extends Card {

    public Warpath() {
        addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(3,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsBlockingPredicate(),
                        new PermanentIsBlockedPredicate())),
                EachPermanentScope.ALL_PLAYERS));
    }
}
