package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSourceAndTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "48")
public class PerplexingChimera extends Card {

    public PerplexingChimera() {
        addTrigger(Triggers.opponentCastsSpell(), new MayEffect(
                new SpellCastTriggerEffect(null, List.of(new ExchangeControlOfSourceAndTriggeringSpellEffect())),
                "Exchange control of this creature and that spell?"));
    }
}
