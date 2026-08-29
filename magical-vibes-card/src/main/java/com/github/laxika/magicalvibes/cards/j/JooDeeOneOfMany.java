package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "105")
public class JooDeeOneOfMany extends Card {

    public JooDeeOneOfMany() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new SurveilEffect(1),
                        new CreateTokenCopyOfSourceEffect(),
                        new SacrificePermanentsEffect(
                                1,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                SacrificeRecipient.CONTROLLER)
                ),
                "{B}, {T}: Surveil 1. Create a token that's a copy of this creature, then sacrifice an artifact or creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
