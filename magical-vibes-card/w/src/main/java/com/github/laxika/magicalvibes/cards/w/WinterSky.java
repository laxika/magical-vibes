package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "HML", collectorNumber = "80")
public class WinterSky extends Card {

    public WinterSky() {
        addSpellEffect(new FlipCoinWinEffect(
                new MassDamageEffect(1, true),
                new EachPlayerDrawsCardEffect(1)));
    }
}
