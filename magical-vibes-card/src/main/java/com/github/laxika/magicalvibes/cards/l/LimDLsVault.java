package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LimDulsVaultEffect;

@CardRegistration(set = "ALL", collectorNumber = "107")
public class LimDLsVault extends Card {

    public LimDLsVault() {
        // Look at the top five cards of your library. As many times as you choose, you may pay
        // 1 life, put those cards on the bottom of your library in any order, then look at the
        // top five cards of your library. Then shuffle and put the last cards you looked at
        // this way on top in any order.
        addEffect(EffectSlot.SPELL, new LimDulsVaultEffect());
    }
}
