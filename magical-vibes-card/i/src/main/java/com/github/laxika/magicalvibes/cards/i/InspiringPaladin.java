package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "FDN", collectorNumber = "18")
public class InspiringPaladin extends Card {

    public InspiringPaladin() {
        addStaticEffect(new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.source())
        ));
        addStaticEffect(new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(
                        Keyword.FIRST_STRIKE,
                        Subjects.creaturesYouControl(),
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                )
        ));
    }
}
