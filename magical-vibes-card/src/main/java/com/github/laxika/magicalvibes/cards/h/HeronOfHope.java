package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "18")
public class HeronOfHope extends Card {

    public HeronOfHope() {
        addEffect(EffectSlot.STATIC, new AdditionalLifeGainEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{1}{W}: This creature gains lifelink until end of turn."
        ));
    }
}
