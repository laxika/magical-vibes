package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "221")
public class ThroneOfEmpires extends Card {

    public ThroneOfEmpires() {
        // {1}, {T}: Create a 1/1 white Soldier creature token. Create five of those tokens
        // instead if you control artifacts named Crown of Empires and Scepter of Empires.
        final Condition hasBothPartners = new AllOf(List.of(
                new ControlsPermanent(artifactNamed("Crown of Empires")),
                new ControlsPermanent(artifactNamed("Scepter of Empires"))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new ConditionalEffect(new NotCondition(hasBothPartners),
                                CreateTokenEffect.whiteSoldier(1)),
                        new ConditionalEffect(hasBothPartners,
                                CreateTokenEffect.whiteSoldier(5))
                ),
                "{1}, {T}: Create a 1/1 white Soldier creature token. Create five of those tokens "
                        + "instead if you control artifacts named Crown of Empires and Scepter of Empires."
        ));
    }

    private static PermanentPredicate artifactNamed(final String name) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNamedPredicate(name)
        ));
    }
}
