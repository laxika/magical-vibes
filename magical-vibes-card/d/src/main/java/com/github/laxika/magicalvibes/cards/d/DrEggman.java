package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.CardsInHandMatchingAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2084")
public class DrEggman extends Card {

    private static final String PUT_LABEL = "Construct, Robot, or Vehicle";

    public DrEggman() {
        CardPredicate constructRobotOrVehicle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.CONSTRUCT),
                new CardSubtypePredicate(CardSubtype.ROBOT),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new DrawCardEffect(1),
                TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.eachOpponent(), List.of(
                        new ChooseOneEffect.ChooseOneOption("Discard a card",
                                new DiscardEffect(1, Subjects.targetPlayerDeclaredElsewhere())),
                        // An option that is impossible may still be chosen and then does nothing (CR 701.55b).
                        new ChooseOneEffect.ChooseOneOption(
                                "Let the controller put a " + PUT_LABEL + " card onto the battlefield",
                                new ConditionalEffect(new CardsInHandMatchingAtLeast(1, constructRobotOrVehicle),
                                        new MayEffect(
                                                new PutCardToBattlefieldEffect(constructRobotOrVehicle, PUT_LABEL),
                                                "Put a " + PUT_LABEL + " card onto the battlefield?")))))));
    }
}
