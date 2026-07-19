package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "CON", collectorNumber = "42")
public class DragDown extends Card {

    public DragDown() {
        // Domain — Target creature gets -1/-1 until end of turn for each basic land type
        // among lands you control. Both P and T shrink by the Domain count (CR 702.42).
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Scaled(new BasicLandTypesAmongControlledLands(), -1),
                new Scaled(new BasicLandTypesAmongControlledLands(), -1)));
    }
}
