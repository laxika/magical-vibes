package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "97")
public class OverzealousMuscle extends Card {

    public OverzealousMuscle() {
        addTrigger(Triggers.youCommitCrime(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF));
    }
}
