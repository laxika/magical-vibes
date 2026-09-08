package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;

@CardRegistration(set = "AKR", collectorNumber = "73")
@CardRegistration(set = "FUT", collectorNumber = "42")
public class PactOfNegation extends Card {

    public PactOfNegation() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new RegisterPayManaOrLoseGameAtNextUpkeepEffect("{3}{U}{U}"));
    }
}
