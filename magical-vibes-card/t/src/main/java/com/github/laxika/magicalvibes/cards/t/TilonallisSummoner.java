package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "121")
public class TilonallisSummoner extends Card {

    public TilonallisSummoner() {
        addStaticEffect(new AscendEffect());

        CreateTokenEffect elementalToken = new CreateTokenEffect(
                1, "Elemental", 1, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), true);
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                UnlessPaysEffect.youMayPayX("{X}{R}", elementalToken.withAmount(new XValue())),
                new ConditionalEffect(HappenedThisWay.created(),
                        RegisterDelayedStepTriggerEffect.aboutCreated(Triggers.endStep(ActivePlayer.ANY),
                                new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                                        new ExileTargetPermanentEffect(Subjects.createdThisResolution()))))));
    }
}
