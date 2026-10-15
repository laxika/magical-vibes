package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M12", collectorNumber = "204")
public class CrumblingColossus extends Card {

    public CrumblingColossus() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
    }
}
