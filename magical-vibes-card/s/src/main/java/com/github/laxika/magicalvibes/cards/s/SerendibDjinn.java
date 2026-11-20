package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "19")
@CardRegistration(set = "ME4", collectorNumber = "61")
public class SerendibDjinn extends Card {

    public SerendibDjinn() {
        // At the beginning of your upkeep, sacrifice a land. If you sacrifice an Island this way,
        // this creature deals 3 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()),
                new ConditionalEffect(
                        HappenedThisWay.toA(StepKind.SACRIFICE, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                        new DealDamageToPlayersEffect(3, Subjects.you()))));

        // When you control no lands, sacrifice this creature. State-triggered ability.
        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsLandPredicate()))),
                new SacrificeSelfEffect());
    }
}
