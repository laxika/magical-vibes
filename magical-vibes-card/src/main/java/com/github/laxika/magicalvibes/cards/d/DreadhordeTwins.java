package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "126")
public class DreadhordeTwins extends Card {

    public DreadhordeTwins() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));

        PermanentPredicate zombieToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, zombieToken));
    }
}
