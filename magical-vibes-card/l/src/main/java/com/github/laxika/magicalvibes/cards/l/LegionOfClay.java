package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "14")
public class LegionOfClay extends Card {

    public LegionOfClay() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PerpetuallyBoostControlledCreaturesEffect(1, 1));
    }
}
