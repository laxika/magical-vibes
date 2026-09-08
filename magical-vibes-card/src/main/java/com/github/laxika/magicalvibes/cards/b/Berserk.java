package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepIfAttackedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ME1", collectorNumber = "114")
@CardRegistration(set = "V09", collectorNumber = "2")
public class Berserk extends Card {

    public Berserk() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_COMBAT_DAMAGE);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)))
                .addEffect(EffectSlot.SPELL,
                        new DestroyTargetPermanentAtEndStepIfAttackedEffect());
    }
}
