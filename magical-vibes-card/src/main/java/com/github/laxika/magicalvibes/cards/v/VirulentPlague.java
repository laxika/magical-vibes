package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "DTK", collectorNumber = "125")
public class VirulentPlague extends Card {

    public VirulentPlague() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ALL_CREATURES,
                new PermanentIsTokenPredicate()));
    }
}
