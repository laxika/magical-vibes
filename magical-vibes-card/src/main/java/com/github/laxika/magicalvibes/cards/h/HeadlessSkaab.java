package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

@CardRegistration(set = "DKA", collectorNumber = "40")
public class HeadlessSkaab extends Card {

    public HeadlessSkaab() {
        addEffect(EffectSlot.SPELL, new ExileNCardsFromGraveyardCost(1, CardType.CREATURE));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
