package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnTopOfLibraryAtEndOfCombatEffect;

@CardRegistration(set = "MMQ", collectorNumber = "101")
public class SaprazzanOutrigger extends Card {

    public SaprazzanOutrigger() {
        addEffect(EffectSlot.ON_ATTACK, new PutSelfOnTopOfLibraryAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new PutSelfOnTopOfLibraryAtEndOfCombatEffect());
    }
}
