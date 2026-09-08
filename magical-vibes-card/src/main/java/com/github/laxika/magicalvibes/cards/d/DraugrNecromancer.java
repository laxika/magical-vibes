package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithIceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;

@CardRegistration(set = "KHM", collectorNumber = "86")
public class DraugrNecromancer extends Card {

    public DraugrNecromancer() {
        addEffect(EffectSlot.STATIC,
                ExileOpponentCreaturesInsteadOfDyingEffect.withIceCounter());
        addEffect(EffectSlot.STATIC,
                new AllowCastFromCardsExiledWithIceCountersEffect(true));
    }
}
