package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "99")
public class MercenaryKnight extends Card {

    public MercenaryKnight() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE));
    }
}
