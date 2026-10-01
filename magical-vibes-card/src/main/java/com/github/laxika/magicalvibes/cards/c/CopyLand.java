package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "47")
@CardRegistration(set = "M3C", collectorNumber = "99")
public class CopyLand extends Card {

    public CopyLand() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsLandPredicate(), "land", Set.of(CardType.ENCHANTMENT), false
        ));
    }
}
