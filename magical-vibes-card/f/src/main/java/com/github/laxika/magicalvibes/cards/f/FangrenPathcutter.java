package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "87")
public class FangrenPathcutter extends Card {

    public FangrenPathcutter() {
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(
                Keyword.TRAMPLE, Subjects.allCreatures(), new PermanentIsAttackingPredicate()
        ));
    }
}
