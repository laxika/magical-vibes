package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;

@CardRegistration(set = "WHO", collectorNumber = "62")
@CardRegistration(set = "WHO", collectorNumber = "667")
public class WibblyWobblyTimeyWimey extends Card {

    public WibblyWobblyTimeyWimey() {
        addSpellEffect(new TimeTravelEffect(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
