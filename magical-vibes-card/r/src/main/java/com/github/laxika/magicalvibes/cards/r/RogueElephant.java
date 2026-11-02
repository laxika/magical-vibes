package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "139")
public class RogueElephant extends Card {

    public RogueElephant() {
        // When this creature enters, sacrifice it unless you sacrifice a Forest.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), "a Forest"));
    }
}
