package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;

@CardRegistration(set = "FUT", collectorNumber = "8")
public class InterventionPact extends Card {

    public InterventionPact() {
        addSpellEffect(PreventDamageFromChosenSourceEffect.nextDamageToYouAndGainLife());
        addSpellEffect(new RegisterPayManaOrLoseGameAtNextUpkeepEffect("{1}{W}{W}"));
    }
}
