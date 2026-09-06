package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "TLA", collectorNumber = "250")
public class WanderingMusicians extends Card {

    public WanderingMusicians() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
