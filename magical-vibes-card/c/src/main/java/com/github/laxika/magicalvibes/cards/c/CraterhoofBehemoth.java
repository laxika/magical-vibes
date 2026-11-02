package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "172")
@CardRegistration(set = "INR", collectorNumber = "480")
@CardRegistration(set = "TDM", collectorNumber = "138")
@CardRegistration(set = "MM3", collectorNumber = "122")
@CardRegistration(set = "SLD", collectorNumber = "375")
@CardRegistration(set = "SLD", collectorNumber = "376")
@CardRegistration(set = "CMM", collectorNumber = "280")
@CardRegistration(set = "CMM", collectorNumber = "556")
public class CraterhoofBehemoth extends Card {

    public CraterhoofBehemoth() {
        // When this creature enters, creatures you control gain trample and get +X/+X until end of
        // turn, where X is the number of creatures you control (this one included).
        var creatureCount = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(creatureCount, creatureCount));
    }
}
