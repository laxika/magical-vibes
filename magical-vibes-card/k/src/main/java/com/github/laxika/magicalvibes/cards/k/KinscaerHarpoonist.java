package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "41")
public class KinscaerHarpoonist extends Card {

    public KinscaerHarpoonist() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new MayEffect(
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        "Have target creature lose flying until end of turn?"
                ));
    }
}
