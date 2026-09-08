package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureByCastSpellManaValueEffect;

@CardRegistration(set = "USG", collectorNumber = "109")
public class VeiledSentry extends Card {

    public VeiledSentry() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new BecomeCreatureByCastSpellManaValueEffect());
    }
}
