package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "153")
public class TemurCharger extends Card {

    public TemurCharger() {
        addMorphWithRevealCost(new CardColorPredicate(CardColor.GREEN), "green");
        target(TargetFilters.creature())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
