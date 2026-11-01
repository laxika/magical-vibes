package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "46")
public class MistfireWeaver extends Card {

    public MistfireWeaver() {
        addMorph("{2}{U}");
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
