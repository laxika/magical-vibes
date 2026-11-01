package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "134")
public class Woodcloaker extends Card {

    public Woodcloaker() {
        addMorph("{2}{G}{G}");
        target(TargetFilters.creature())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
