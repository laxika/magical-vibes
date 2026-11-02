package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "259")
public class PucasEye extends Card {

    public PucasEye() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseColorForSourceEffect());
        addStaticEffect(new BecomeChosenColorEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DrawCardEffect()),
                "{3}, {T}: Draw a card. Activate only if there are five colors among permanents you control."
        ).withActivationCondition(
                new AllConditions(List.of(
                        controlsColor(CardColor.WHITE),
                        controlsColor(CardColor.BLUE),
                        controlsColor(CardColor.BLACK),
                        controlsColor(CardColor.RED),
                        controlsColor(CardColor.GREEN)
                )),
                "Activate only if there are five colors among permanents you control"));
    }

    private static ControlsPermanentCount controlsColor(CardColor color) {
        return new ControlsPermanentCount(1, new PermanentColorInPredicate(Set.of(color)));
    }
}
