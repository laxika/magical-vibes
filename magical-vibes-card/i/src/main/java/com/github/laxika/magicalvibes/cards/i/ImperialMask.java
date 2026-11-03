package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "23")
public class ImperialMask extends Card {

    public ImperialMask() {
        // "You have hexproof."
        addStaticEffect(new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        // "Each of your teammates creates a token copy" — the engine has no team games, so there are
        // zero teammates; the trigger's "if it isn't a token" clause is an intervening-if, so a token
        // never triggers while a nontoken still puts the trigger on the stack and creates nothing.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsToken())),
                new CreateTokenCopyOfSourceEffect(false, 0));
    }
}
