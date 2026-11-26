package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TDM", collectorNumber = "20")
public class RebelliousStrike extends Card {

    public RebelliousStrike() {
        addSpellEffect(BoostEffect.target(3, 0));
        addSpellEffect(new DrawCardEffect(1));
    }
}
