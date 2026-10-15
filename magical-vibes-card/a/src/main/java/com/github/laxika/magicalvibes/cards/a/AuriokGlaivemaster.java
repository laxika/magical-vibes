package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "1")
public class AuriokGlaivemaster extends Card {

    public AuriokGlaivemaster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Equipped(),
                new StaticBoostEffect(1, 1, Set.of(Keyword.FIRST_STRIKE), GrantScope.SELF)
        ));
    }
}
