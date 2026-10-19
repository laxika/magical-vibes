package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;


@CardRegistration(set = "HOU", collectorNumber = "108")
@CardRegistration(set = "AKR", collectorNumber = "177")
public class ThornedMoloch extends Card {

    public ThornedMoloch() {
        // This creature has first strike as long as it's attacking.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttacking(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
    }
}
