package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "32")
public class RapidFire extends Card {

    public RapidFire() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_BLOCKERS_DECLARED);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                        Keyword.FIRST_STRIKE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetEffect(
                        EffectSlot.ON_BECOMES_BLOCKED,
                        new RampageEffect(2),
                        EffectDuration.UNTIL_END_OF_TURN));
    }
}
