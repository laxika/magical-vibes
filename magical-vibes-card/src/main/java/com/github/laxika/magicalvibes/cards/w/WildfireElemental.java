package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "M20", collectorNumber = "297")
public class WildfireElemental extends Card {

    public WildfireElemental() {
        addEffect(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
