package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;


@CardRegistration(set = "DSK", collectorNumber = "49")
public class DontMakeASound extends Card {

    public DontMakeASound() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(2).ifPaid(new SurveilEffect(2)));
    }
}
