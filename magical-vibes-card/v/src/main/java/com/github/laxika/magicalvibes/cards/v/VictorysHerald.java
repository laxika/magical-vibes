package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "18")
@CardRegistration(set = "W17", collectorNumber = "6")
@CardRegistration(set = "C15", collectorNumber = "83")
@CardRegistration(set = "CM2", collectorNumber = "38")
public class VictorysHerald extends Card {

    public VictorysHerald() {
        // Whenever Victory's Herald attacks, attacking creatures gain flying and lifelink until end of turn.
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(
                Keyword.FLYING, Subjects.allCreatures(), new PermanentIsAttackingPredicate()
        ),
                new GrantKeywordEffect(
                Keyword.LIFELINK, Subjects.allCreatures(), new PermanentIsAttackingPredicate()
        ));
    }
}
