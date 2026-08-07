package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WTH", collectorNumber = "139")
public class RogueElephant extends Card {

    public RogueElephant() {
        // When this creature enters, sacrifice it unless you sacrifice a Forest.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), "a Forest"));
    }
}
