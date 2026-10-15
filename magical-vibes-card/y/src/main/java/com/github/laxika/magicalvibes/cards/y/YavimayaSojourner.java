package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "DMU", collectorNumber = "191")
public class YavimayaSojourner extends Card {

    public YavimayaSojourner() {
        // Domain — This spell costs {1} less to cast for each basic land type among lands you control.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new BasicLandTypesAmongControlledLands()));
    }
}
