package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "69")
public class RakdosPitDragon extends Card {

    public RakdosPitDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{R}{R}: This creature gains flying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)
        ));
    }
}
