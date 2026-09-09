package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerCantCastSpellsThisTurnEffect;

@CardRegistration(set = "SCG", collectorNumber = "135")
public class XantidSwarm extends Card {

    public XantidSwarm() {
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerCantCastSpellsThisTurnEffect());
    }
}
