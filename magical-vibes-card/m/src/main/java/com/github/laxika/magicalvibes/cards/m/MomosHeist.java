package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "72")
public class MomosHeist extends Card {

    public MomosHeist() {
        target(TargetFilters.artifact())
                .addSpellEffect(new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET, GrantDuration.INDEFINITE))
                .addSpellEffect(new SacrificeTargetPermanentAtEndStepEffect(false, true));
    }
}
