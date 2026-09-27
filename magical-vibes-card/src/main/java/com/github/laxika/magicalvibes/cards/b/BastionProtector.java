package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "122")
@CardRegistration(set = "MSC", collectorNumber = "296")
@CardRegistration(set = "C15", collectorNumber = "1")
@CardRegistration(set = "LTC", collectorNumber = "162")
public class BastionProtector extends Card {

    public BastionProtector() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2, 2, Set.of(Keyword.INDESTRUCTIBLE), GrantScope.ALL_OWN_CREATURES,
                new PermanentIsCommanderPredicate()));
    }
}
