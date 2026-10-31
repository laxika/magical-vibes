package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithIceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;

@CardRegistration(set = "KHM", collectorNumber = "86")
@CardRegistration(set = "KHM", collectorNumber = "351")
public class DraugrNecromancer extends Card {

    public DraugrNecromancer() {
        addStaticEffect(ExileOpponentCreaturesInsteadOfDyingEffect.withIceCounter());
        addStaticEffect(new AllowCastFromCardsExiledWithIceCountersEffect(true));
    }
}
