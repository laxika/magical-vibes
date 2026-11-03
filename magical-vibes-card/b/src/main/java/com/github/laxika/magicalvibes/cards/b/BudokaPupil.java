package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.IchigaWhoTopplesOaks;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "122")
public class BudokaPupil extends Card {

    public BudokaPupil() {
        setBackFaceCard(new IchigaWhoTopplesOaks());

        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE)))),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.KI), "Put a ki counter on Budoka Pupil?"));

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(2, CounterType.KI)),
                new MayEffect(new TransformToBackFaceEffect(true), "Flip Budoka Pupil?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "IchigaWhoTopplesOaks";
    }
}
