package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;

@CardRegistration(set = "MBS", collectorNumber = "126")
public class PsychosisCrawler extends Card {

    public PsychosisCrawler() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInHandEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new EachOpponentLosesLifeEffect(1));
    }
}
