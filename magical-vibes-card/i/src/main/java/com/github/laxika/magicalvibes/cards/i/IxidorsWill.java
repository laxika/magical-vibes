package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "90")
public class IxidorsWill extends Card {

    public IxidorsWill() {
        addSpellEffect(new CounterUnlessPaysEffect(new Scaled(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.WIZARD), CountScope.ANY_PLAYER), 2)));
    }
}
