package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostBlockerWhenAttackerMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "46")
public class RighteousIndignation extends Card {

    public RighteousIndignation() {
        addEffect(EffectSlot.ON_ANY_CREATURE_BECOMES_BLOCKED,
                new BoostBlockerWhenAttackerMatchesEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)), 1, 1));
    }
}
