package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "136")
@CardRegistration(set = "DDH", collectorNumber = "67")
@CardRegistration(set = "E01", collectorNumber = "86")
public class SlaveOfBolas extends Card {

    public SlaveOfBolas() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addSpellEffect(new SacrificeTargetPermanentAtEndStepEffect(false, true));
    }
}
