package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "202")
public class Lithophage extends Card {

    public Lithophage() {
        // At the beginning of your upkeep, sacrifice this creature unless you sacrifice a Mountain.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), "a Mountain"));
    }
}
