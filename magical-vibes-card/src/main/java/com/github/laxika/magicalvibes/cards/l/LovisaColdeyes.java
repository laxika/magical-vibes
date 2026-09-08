package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "90")
public class LovisaColdeyes extends Card {

    public LovisaColdeyes() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, Set.of(Keyword.HASTE),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BARBARIAN, CardSubtype.WARRIOR,
                        CardSubtype.BERSERKER))));
    }
}
