package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "203")
public class CrownOfEmpires extends Card {

    public CrownOfEmpires() {
        // {3}, {T}: Tap target creature. Gain control of that creature instead if you
        // control artifacts named Scepter of Empires and Throne of Empires.
        final Condition hasBothPartners = new AllOf(List.of(
                new ControlsPermanent(artifactNamed("Scepter of Empires")),
                new ControlsPermanent(artifactNamed("Throne of Empires"))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new ConditionalEffect(new NotCondition(hasBothPartners),
                                new TapPermanentsEffect(TapUntapScope.TARGET)),
                        new ConditionalEffect(hasBothPartners,
                                new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                ),
                "{3}, {T}: Tap target creature. Gain control of that creature instead if you control "
                        + "artifacts named Scepter of Empires and Throne of Empires.",
                TargetFilters.creature()
        ));
    }

    private static PermanentPredicate artifactNamed(final String name) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNamedPredicate(name)
        ));
    }
}
