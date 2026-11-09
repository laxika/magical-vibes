package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueParityMatchesSourceChosenParityPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "70")
public class GollumRiddleMaster extends Card {

    public GollumRiddleMaster() {
        addStaticEffect(new ChooseManaValueParityOnEnterEffect());
        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryManaValueParityMatchesSourceChosenParityPredicate()),
                ChooseOneEffect.notYetChosen(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on Gollum",
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Each opponent loses 2 life and you gain 2 life",
                                List.of(
                                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                        new GainLifeEffect(2))),
                        new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect()))));
    }
}
