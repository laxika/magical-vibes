package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "7")
@CardRegistration(set = "GNT", collectorNumber = "9")
public class BrightReprisal extends Card {

    public BrightReprisal() {
        target(TargetFilters.attackingCreature()).addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
