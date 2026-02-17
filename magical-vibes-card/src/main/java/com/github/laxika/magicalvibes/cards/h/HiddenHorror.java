package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;

public class HiddenHorror extends Card {

    public HiddenHorror() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE));
    }
}
