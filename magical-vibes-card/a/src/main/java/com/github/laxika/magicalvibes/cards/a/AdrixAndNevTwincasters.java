package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1544")
@CardRegistration(set = "SPG", collectorNumber = "156")
@CardRegistration(set = "C21", collectorNumber = "9")
@CardRegistration(set = "MKC", collectorNumber = "198")
public class AdrixAndNevTwincasters extends Card {

    public AdrixAndNevTwincasters() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addStaticEffect(new MultiplyTokenCreationEffect(2));
    }
}
