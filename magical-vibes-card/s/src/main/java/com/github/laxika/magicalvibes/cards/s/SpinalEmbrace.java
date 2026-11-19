package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "276")
@CardRegistration(set = "DMR", collectorNumber = "201")
@CardRegistration(set = "C13", collectorNumber = "218")
@CardRegistration(set = "ZNC", collectorNumber = "101")
public class SpinalEmbrace extends Card {

    public SpinalEmbrace() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.ONLY_DURING_COMBAT);

        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GainControlOfTargetEffect(Duration.PERMANENT))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addSpellEffect(new SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect());
    }
}
