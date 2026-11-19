package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "172")
public class Goatnapper extends Card {

    public Goatnapper() {
        // When this creature enters, untap target Goat and gain control of it until end
        // of turn. It gains haste until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.GOAT),
                "Target must be a Goat"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new UntapPermanentsEffect(Subjects.target()),
                        new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
