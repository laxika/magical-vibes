package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "223")
public class AtraxasSkitterfang extends Card {

    public AtraxasSkitterfang() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new Fixed(3)));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new RemoveCounterFromSourceThenEffect(CounterType.OIL,
                        new GrantChosenKeywordEffect(
                                List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DEATHTOUCH, Keyword.LIFELINK),
                                GrantScope.TARGET,
                                new PermanentControlledBySourceControllerPredicate())),
                "Remove an oil counter from Atraxa's Skitterfang?"));
    }
}
