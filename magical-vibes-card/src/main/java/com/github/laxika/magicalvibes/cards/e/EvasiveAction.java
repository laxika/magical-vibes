package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "APC", collectorNumber = "23")
public class EvasiveAction extends Card {

    public EvasiveAction() {
        // Domain — Counter target spell unless its controller pays {1} for each basic land type
        // among lands you control.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new BasicLandTypesAmongControlledLands()));
    }
}
