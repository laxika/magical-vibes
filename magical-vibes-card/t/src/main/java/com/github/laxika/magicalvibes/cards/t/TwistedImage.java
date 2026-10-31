package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "SOM", collectorNumber = "50")
@CardRegistration(set = "A25", collectorNumber = "75")
public class TwistedImage extends Card {

    public TwistedImage() {
        addSpellEffect(new SwitchPowerToughnessEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
