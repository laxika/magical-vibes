package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HML", collectorNumber = "80")
public class WinterSky extends Card {

    public WinterSky() {
        addSpellEffect(new FlipCoinWinEffect(
                DealDamageEffect.toEachCreature(1).andTo(Subjects.eachPlayer()),
                new EachPlayerDrawsCardEffect(1)));
    }
}
