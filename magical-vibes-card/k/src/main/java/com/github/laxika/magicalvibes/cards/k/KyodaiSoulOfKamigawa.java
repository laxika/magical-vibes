package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "23")
public class KyodaiSoulOfKamigawa extends Card {

    public KyodaiSoulOfKamigawa() {
        PermanentPredicate another = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        target(new PermanentPredicateTargetFilter(another, "Target must be another permanent"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new GrantKeywordEffect(
                        Set.of(Keyword.INDESTRUCTIBLE), Subjects.target(), another,
                        Duration.WHILE_SOURCE_ON_BATTLEFIELD, null));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(new BoostSelfEffect(5, 5)),
                "{W}{U}{B}{R}{G}: Kyodai gets +5/+5 until end of turn."
        ));
    }
}
