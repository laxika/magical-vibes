package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MOR", collectorNumber = "87")
public class BorderlandBehemoth extends Card {

    public BorderlandBehemoth() {
        // Trample (from Scryfall metadata).
        // This creature gets +4/+4 for each other Giant you control.
        Scaled fourPerGiant = new Scaled(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GIANT),
                CountScope.CONTROLLER, true), 4);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(fourPerGiant, fourPerGiant));
    }
}
