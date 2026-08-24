package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "RNA", collectorNumber = "3")
public class ArchwayAngel extends Card {

    public ArchwayAngel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER), 2)));
    }
}
