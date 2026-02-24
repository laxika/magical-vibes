package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandEffect;

@CardRegistration(set = "10E", collectorNumber = "172")
public class Recover extends Card {

    public Recover() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardToHandEffect(CardType.CREATURE));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
