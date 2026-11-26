package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "17")
public class HauntingWind extends Card {

    public HauntingWind() {
        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped()
                                .withFilter(new PermanentIsArtifactPredicate()),
                        Triggers.permanentOpponentControlsBecomesTapped()
                                .withFilter(new PermanentIsArtifactPredicate())),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 1));

        CardEffect artifactActivationTrigger =
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 1);
        addTrigger(Triggers.youActivateAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsArtifactPredicate()),
                artifactActivationTrigger);
        addTrigger(Triggers.opponentActivatesAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsArtifactPredicate()),
                artifactActivationTrigger);
    }
}
