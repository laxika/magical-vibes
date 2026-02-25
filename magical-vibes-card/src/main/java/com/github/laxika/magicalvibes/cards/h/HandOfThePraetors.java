package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "66")
public class HandOfThePraetors extends Card {

    public HandOfThePraetors() {
        // Other creatures you control with infect get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.INFECT)));

        // Whenever you cast a creature spell with infect, target player gets a poison counter.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new GiveTargetPlayerPoisonCountersEffect(1, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.INFECT)
                ))));
    }
}
