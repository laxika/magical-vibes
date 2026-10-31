package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "166")
@CardRegistration(set = "M19", collectorNumber = "161")
@CardRegistration(set = "FDN", collectorNumber = "209")
@CardRegistration(set = "M21", collectorNumber = "163")
@CardRegistration(set = "GRN", collectorNumber = "118")
@CardRegistration(set = "BFZ", collectorNumber = "157")
@CardRegistration(set = "VOW", collectorNumber = "179")
@CardRegistration(set = "DBL", collectorNumber = "446")
public class SureStrike extends Card {

    public SureStrike() {
        addSpellEffect(new BoostTargetCreatureEffect(3, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
