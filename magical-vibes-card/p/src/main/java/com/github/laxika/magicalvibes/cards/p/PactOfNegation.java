package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;

@CardRegistration(set = "AKR", collectorNumber = "73")
@CardRegistration(set = "FUT", collectorNumber = "42")
@CardRegistration(set = "MMA", collectorNumber = "56")
@CardRegistration(set = "MP2", collectorNumber = "16")
@CardRegistration(set = "A25", collectorNumber = "68")
@CardRegistration(set = "TSR", collectorNumber = "77")
@CardRegistration(set = "MB2", collectorNumber = "169")
@CardRegistration(set = "LTC", collectorNumber = "523")
public class PactOfNegation extends Card {

    public PactOfNegation() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new RegisterPayManaOrLoseGameAtNextUpkeepEffect("{3}{U}{U}"));
    }
}
