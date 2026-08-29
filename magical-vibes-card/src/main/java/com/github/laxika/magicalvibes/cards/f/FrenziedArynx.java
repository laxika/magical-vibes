package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "173")
public class FrenziedArynx extends Card {

    public FrenziedArynx() {
        addEffect(EffectSlot.STATIC, new RiotEffect());

        // {4}{R}{G}: This creature gets +3/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}{G}",
                List.of(new BoostSelfEffect(3, 0)),
                "{4}{R}{G}: This creature gets +3/+0 until end of turn."
        ));
    }
}
