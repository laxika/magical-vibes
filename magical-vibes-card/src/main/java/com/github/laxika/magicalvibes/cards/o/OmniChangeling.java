package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "62")
public class OmniChangeling extends Card {

    public OmniChangeling() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                Map.of(EffectSlot.STATIC, List.<CardEffect>of(
                        new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.SELF)))
        ));
    }
}
