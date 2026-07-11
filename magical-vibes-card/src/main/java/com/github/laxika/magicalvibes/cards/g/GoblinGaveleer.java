package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "SOM", collectorNumber = "92")
public class GoblinGaveleer extends Card {

    public GoblinGaveleer() {
        // Goblin Gaveleer gets +2/+0 for each Equipment attached to it.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Scaled(new AttachmentsOnSource(false, true), 2),
                new Fixed(0)));
    }
}
