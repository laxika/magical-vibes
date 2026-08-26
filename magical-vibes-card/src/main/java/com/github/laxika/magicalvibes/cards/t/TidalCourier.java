package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "31")
public class TidalCourier extends Card {

    public TidalCourier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsMatchingToHandRestToBottomEffect(
                4, new CardSubtypePredicate(CardSubtype.MERFOLK)));
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{3}{U}: This creature gains flying until end of turn."));
    }
}
