package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "M21", collectorNumber = "138")
public class ChandrasPyreling extends Card {

    public ChandrasPyreling() {
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
    }
}
