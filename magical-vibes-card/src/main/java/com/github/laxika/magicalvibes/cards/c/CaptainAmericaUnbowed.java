package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "505")
public class CaptainAmericaUnbowed extends Card {

    public CaptainAmericaUnbowed() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER, CardSubtype.HERO))));
    }
}
