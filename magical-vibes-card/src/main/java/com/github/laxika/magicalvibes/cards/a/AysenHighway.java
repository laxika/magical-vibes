package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "5")
public class AysenHighway extends Card {

    public AysenHighway() {
        // White creatures have plainswalk.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.PLAINSWALK, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
