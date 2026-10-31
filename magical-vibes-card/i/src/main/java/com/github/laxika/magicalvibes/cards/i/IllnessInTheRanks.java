package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "GTC", collectorNumber = "69")
@CardRegistration(set = "PIO", collectorNumber = "292")
public class IllnessInTheRanks extends Card {

    public IllnessInTheRanks() {
        addStaticEffect(new StaticBoostEffect(-1, -1, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentIsTokenPredicate()));
    }
}
