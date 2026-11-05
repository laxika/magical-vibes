package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "206")
@CardRegistration(set = "DDN", collectorNumber = "17")
@CardRegistration(set = "EMA", collectorNumber = "201")
@CardRegistration(set = "MB1", collectorNumber = "201")
public class FlameKinZealot extends Card {

    public FlameKinZealot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(1, 1),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
    }
}
