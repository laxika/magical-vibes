package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

@CardRegistration(set = "RTR", collectorNumber = "131")
public class OakStreetInnkeeper extends Card {

    public OakStreetInnkeeper() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotControllerTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES,
                        new PermanentIsTappedPredicate())));
    }
}
