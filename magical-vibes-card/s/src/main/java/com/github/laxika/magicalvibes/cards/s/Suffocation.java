package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DealtDamageByRedSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToLastRedSpellDamagerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ALL", collectorNumber = "38")
public class Suffocation extends Card {

    public Suffocation() {
        setCastCondition(new DealtDamageByRedSpellThisTurn());
        addEffect(EffectSlot.SPELL, new DealDamageToLastRedSpellDamagerEffect(4));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
