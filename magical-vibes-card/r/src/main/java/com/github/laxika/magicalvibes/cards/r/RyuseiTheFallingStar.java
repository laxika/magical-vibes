package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "185")
@CardRegistration(set = "MMA", collectorNumber = "128")
@CardRegistration(set = "IMA", collectorNumber = "144")
@CardRegistration(set = "C17", collectorNumber = "141")
@CardRegistration(set = "ARC", collectorNumber = "45")
public class RyuseiTheFallingStar extends Card {

    public RyuseiTheFallingStar() {
        // When Ryusei dies, it deals 5 damage to each creature without flying.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MassDamageEffect(
                new Fixed(5), false, false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
