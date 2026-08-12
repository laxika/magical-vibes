package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "CHR", collectorNumber = "79")
public class MarhaultElsdragon extends Card {

    public MarhaultElsdragon() {
        var blockersBeyondTheFirst = new Max(
                new Sum(new CreaturesBlockingSource(), new Fixed(-1)), new Fixed(0));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(blockersBeyondTheFirst, 1),
                new Scaled(blockersBeyondTheFirst, 1)));
    }
}
