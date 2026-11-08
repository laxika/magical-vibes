package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LichDuelMasteryLifeLossReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "310")
@CardRegistration(set = "MB2", collectorNumber = "546")
public class LichsDuelMastery extends Card {

    public LichsDuelMastery() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));
        addStaticEffect(new LichDuelMasteryLifeLossReplacementEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(5, true));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ControllerLosesGameEffect());
    }
}
