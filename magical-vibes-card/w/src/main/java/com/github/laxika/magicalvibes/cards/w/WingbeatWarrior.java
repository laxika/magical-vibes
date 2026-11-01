package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "29")
public class WingbeatWarrior extends Card {

    public WingbeatWarrior() {
        addMorph("{2}{W}");
        target(TargetFilters.creature())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
