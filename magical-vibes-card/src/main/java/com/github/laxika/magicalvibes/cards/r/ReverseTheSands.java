package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedistributePlayerLifeTotalsEffect;

@CardRegistration(set = "CHK", collectorNumber = "41")
public class ReverseTheSands extends Card {

    public ReverseTheSands() {
        addEffect(EffectSlot.SPELL, new RedistributePlayerLifeTotalsEffect());
    }
}
