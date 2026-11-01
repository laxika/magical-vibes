package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "124")
@CardRegistration(set = "DDI", collectorNumber = "56")
public class GeyserGlider extends Card {

    public GeyserGlider() {
        // Landfall — Whenever a land you control enters, this creature gains flying until end of turn.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
