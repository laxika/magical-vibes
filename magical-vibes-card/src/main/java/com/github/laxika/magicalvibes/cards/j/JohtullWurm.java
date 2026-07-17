package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "5ED", collectorNumber = "306")
public class JohtullWurm extends Card {

    public JohtullWurm() {
        // Whenever this creature becomes blocked, it gets -2/-1 until end of turn
        // for each creature blocking it beyond the first, i.e. (blockers - 1) times.
        Sum blockersBeyondFirst = new Sum(new CreaturesBlockingSource(), new Fixed(-1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(blockersBeyondFirst, -2),
                new Scaled(blockersBeyondFirst, -1)));
    }
}
