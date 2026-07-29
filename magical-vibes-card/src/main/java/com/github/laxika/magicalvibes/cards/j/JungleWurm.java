package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "224")
public class JungleWurm extends Card {

    public JungleWurm() {
        // Whenever Jungle Wurm becomes blocked, it gets -1/-1 until end of turn
        // for each creature blocking it beyond the first: -(blockers - 1), floored at 0.
        var blockersBeyondTheFirst = new Scaled(
                new Max(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), new Fixed(0)), -1);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostSelfEffect(blockersBeyondTheFirst, blockersBeyondTheFirst));
    }
}
