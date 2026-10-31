package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DMU", collectorNumber = "65")
public class SilverScrutiny extends Card {

    public SilverScrutiny() {
        setFlashCastCondition(new NotCondition(new SpellXAtLeast(4)));
        addSpellEffect(new DrawCardEffect(new XValue()));
    }
}
