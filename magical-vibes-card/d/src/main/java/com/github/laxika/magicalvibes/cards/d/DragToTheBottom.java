package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "DMU", collectorNumber = "91")
public class DragToTheBottom extends Card {

    public DragToTheBottom() {
        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        Sum debuff = new Sum(new Fixed(1), domain);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(debuff, -1), new Scaled(debuff, -1)));
    }
}
