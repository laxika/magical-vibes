package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "124")
public class FlashConscription extends Card {

    public FlashConscription() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ColorSpentToCast(ManaColor.WHITE),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                                new GainLifeEffect(new EventValue()),
                                EffectDuration.UNTIL_END_OF_TURN)));
    }
}
