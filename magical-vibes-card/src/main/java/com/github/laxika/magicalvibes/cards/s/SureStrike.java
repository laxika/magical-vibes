package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "166")
@CardRegistration(set = "M19", collectorNumber = "161")
@CardRegistration(set = "FDN", collectorNumber = "209")
@CardRegistration(set = "M21", collectorNumber = "163")
@CardRegistration(set = "GRN", collectorNumber = "118")
public class SureStrike extends Card {

    public SureStrike() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 0));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
