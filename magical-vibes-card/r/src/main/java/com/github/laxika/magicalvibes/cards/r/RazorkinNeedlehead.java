package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "153")
public class RazorkinNeedlehead extends Card {

    public RazorkinNeedlehead() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.source())));
        addTrigger(Triggers.opponentDrawsCard(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));
    }
}
