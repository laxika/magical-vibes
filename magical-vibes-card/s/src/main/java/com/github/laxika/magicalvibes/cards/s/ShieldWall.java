package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "44")
@CardRegistration(set = "5ED", collectorNumber = "63")
@CardRegistration(set = "CHR", collectorNumber = "12")
@CardRegistration(set = "LEG", collectorNumber = "36")
public class ShieldWall extends Card {

    public ShieldWall() {
        addSpellEffect(BoostEffect.creaturesYouControl(0, 2));
    }
}
