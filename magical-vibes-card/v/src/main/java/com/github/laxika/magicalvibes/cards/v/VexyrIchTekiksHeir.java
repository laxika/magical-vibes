package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "29")
public class VexyrIchTekiksHeir extends Card {

    public VexyrIchTekiksHeir() {
        CreateTokenEffect golemToken = new CreateTokenEffect(
                "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.ON_CONTROLLER_SEEKS, golemToken);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.VIGILANCE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.GOLEM)));
    }
}
