package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "89")
public class BloodstokeHowler extends Card {

    public BloodstokeHowler() {
        addMorph("{6}{R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(3, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.BEAST)));
    }
}
