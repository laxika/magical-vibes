package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSourceAndTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "48")
public class PerplexingChimera extends Card {

    public PerplexingChimera() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                new MayEffect(new ExchangeControlOfSourceAndTriggeringSpellEffect(), "Exchange control of this creature and that spell?"));
    }
}
