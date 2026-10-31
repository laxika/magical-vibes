package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "137")
@CardRegistration(set = "SLD", collectorNumber = "656")
public class RootSliver extends Card {

    public RootSliver() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new SpellsCantBeCounteredEffect(new CardSubtypePredicate(CardSubtype.SLIVER)));
    }
}
