package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndChooseBlockersEffect;

@CardRegistration(set = "INV", collectorNumber = "171")
public class StandOrFall extends Card {

    public StandOrFall() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new SeparateCreaturesIntoPilesAndChooseBlockersEffect());
    }
}
