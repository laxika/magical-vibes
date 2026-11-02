package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRB", collectorNumber = "14")
@CardRegistration(set = "S99", collectorNumber = "119")
@CardRegistration(set = "DDG", collectorNumber = "61")
@CardRegistration(set = "ME4", collectorNumber = "138")
public class ThunderDragon extends Card {

    public ThunderDragon() {
        // When this creature enters, it deals 3 damage to each creature without flying.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(3, false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
