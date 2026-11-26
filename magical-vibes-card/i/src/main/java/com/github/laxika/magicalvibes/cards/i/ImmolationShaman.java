package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "106")
public class ImmolationShaman extends Card {

    public ImmolationShaman() {
        addTrigger(Triggers.opponentActivatesNonManaAbility()
                        .withSource(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()))),
                DealDamageEffect.to(Subjects.targetPlayer(), 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(
                        BoostEffect.self(3, 3),
                        new GrantKeywordEffect(Keyword.MENACE, Subjects.source())),
                "{3}{R}{R}: This creature gets +3/+3 and gains menace until end of turn."
        ));
    }
}
