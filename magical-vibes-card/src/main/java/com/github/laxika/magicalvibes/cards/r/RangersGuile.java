package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ISD", collectorNumber = "201")
@CardRegistration(set = "M14", collectorNumber = "191")
@CardRegistration(set = "M15", collectorNumber = "193")
@CardRegistration(set = "M21", collectorNumber = "199")
public class RangersGuile extends Card {

    public RangersGuile() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
