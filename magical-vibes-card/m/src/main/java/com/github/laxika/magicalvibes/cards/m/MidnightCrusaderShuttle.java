package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfDefendingPlayerCreatureAndAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "179")
@CardRegistration(set = "WHO", collectorNumber = "784")
public class MidnightCrusaderShuttle extends Card {

    public MidnightCrusaderShuttle() {
        addTrigger(Triggers.attacks(), TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.defendingPlayer(), List.of(
                new ChooseOneEffect.ChooseOneOption("That player sacrifices a creature",
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                Subjects.targetPlayerDeclaredElsewhere())),
                new ChooseOneEffect.ChooseOneOption("You gain control of a creature that player controls",
                        new GainControlOfDefendingPlayerCreatureAndAttackEffect()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
