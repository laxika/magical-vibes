package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureOfEachManaValueToBattlefieldEffect;

@CardRegistration(set = "YDFT", collectorNumber = "23")
public class OrnateImitations extends Card {

    public OrnateImitations() {
        addEffect(EffectSlot.SPELL,
                new ConjureRandomCreatureOfEachManaValueToBattlefieldEffect(new XValue()));
    }
}
