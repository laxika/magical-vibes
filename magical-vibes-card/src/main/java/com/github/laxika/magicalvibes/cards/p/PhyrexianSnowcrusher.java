package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "140")
public class PhyrexianSnowcrusher extends Card {

    public PhyrexianSnowcrusher() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addActivatedAbility(new ActivatedAbility(false, "{1}{S}",
                List.of(new BoostSelfEffect(1, 0)),
                "{1}{S}: This creature gets +1/+0 until end of turn."));
    }
}
