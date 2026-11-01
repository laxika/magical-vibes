package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "9ED", collectorNumber = "214")
@CardRegistration(set = "8ED", collectorNumber = "216")
@CardRegistration(set = "ARN", collectorNumber = "43")
public class RukhEgg extends Card {

    public RukhEgg() {
        // When this creature dies, create a 4/4 red Bird creature token with flying
        // at the beginning of the next end step.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new RegisterDelayedCreateTokenEffect(
                new CreateTokenEffect("Bird", 4, 4, CardColor.RED,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));
    }
}
