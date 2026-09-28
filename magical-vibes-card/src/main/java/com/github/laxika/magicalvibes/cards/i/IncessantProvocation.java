package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "57")
public class IncessantProvocation extends Card {

    public IncessantProvocation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new PerpetuallyGrantStaticEffectToTargetCreatureEffect(new MustAttackEffect()));
    }
}
