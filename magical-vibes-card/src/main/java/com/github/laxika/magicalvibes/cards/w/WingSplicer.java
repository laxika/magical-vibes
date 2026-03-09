package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "50")
public class WingSplicer extends Card {

    public WingSplicer() {
        // When Wing Splicer enters the battlefield, create a 3/3 colorless Phyrexian Golem artifact creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateCreatureTokenEffect(
                "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT)));

        // Golem creatures you control have flying.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.GOLEM)));
    }
}
