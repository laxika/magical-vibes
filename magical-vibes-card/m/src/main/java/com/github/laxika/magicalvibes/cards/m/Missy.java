package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "8")
@CardRegistration(set = "WHO", collectorNumber = "1022")
@CardRegistration(set = "WHO", collectorNumber = "431")
@CardRegistration(set = "WHO", collectorNumber = "546")
@CardRegistration(set = "WHO", collectorNumber = "613")
@CardRegistration(set = "WHO", collectorNumber = "1137")
public class Missy extends Card {

    static final String DAMAGE_OPTION = "Each artifact creature you control deals 1 damage to you";
    static final String DRAW_OPTION = "You draw a card and chaos ensues";

    public Missy() {
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsArtifactPredicate()))),
                        ObjectRelation.ANOTHER),
                new ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.eachOpponent(), List.of(
                        new ChooseOneEffect.ChooseOneOption(DAMAGE_OPTION,
                                new ControlledCreaturesDealDamageToTargetPlayerEffect(1,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsArtifactPredicate())))),
                        new ChooseOneEffect.ChooseOneOption(DRAW_OPTION,
                                SequenceEffect.of(new DrawCardEffect(1), new ChaosEnsuesEffect())))));
    }
}
