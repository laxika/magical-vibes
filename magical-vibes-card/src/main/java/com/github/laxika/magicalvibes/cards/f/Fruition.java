package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "POR", collectorNumber = "166")
public class Fruition extends Card {

    public Fruition() {
        // You gain 1 life for each Forest on the battlefield.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.ANY_PLAYER)));
    }
}
