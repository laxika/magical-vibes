package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "157")
public class BarrelingAttack extends Card {

    public BarrelingAttack() {
        // Target creature gains trample until end of turn. When that creature becomes blocked
        // this turn, it gets +1/+1 until end of turn for each creature blocking it.
        // The becomes-blocked rider is a temporary ON_BECOMES_BLOCKED ability on the target;
        // CreaturesBlockingSource counts the blockers at resolution (Elvish Berserker's amount).
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_BECOMES_BLOCKED,
                        new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())));
    }
}
