package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "130")
public class VitosInquisitor extends Card {

    public VitosInquisitor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate()
                                )),
                                "Sacrifice another creature or artifact"
                        ),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.MENACE, Subjects.source())
                ),
                "{B}, Sacrifice another creature or artifact: Put a +1/+1 counter on this creature. It gains menace until end of turn."
        ));
    }
}
