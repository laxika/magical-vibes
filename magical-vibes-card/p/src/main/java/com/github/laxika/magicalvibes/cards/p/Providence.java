package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOpeningHandUpkeepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

@CardRegistration(set = "EMN", collectorNumber = "37")
public class Providence extends Card {

    public Providence() {
        addSpellEffect(new SetLifeTotalEffect(26));
        addStaticEffect(new MayEffect(
                new RegisterOpeningHandUpkeepTriggerEffect(new SetLifeTotalEffect(26)),
                "Reveal this card from your opening hand?"
        ));
    }
}
