package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "114")
@CardRegistration(set = "WHO", collectorNumber = "403")
@CardRegistration(set = "WHO", collectorNumber = "719")
@CardRegistration(set = "WHO", collectorNumber = "994")
public class TheBeastDeathlessPrince extends Card {

    public TheBeastDeathlessPrince() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.youCastThisSpell(),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addTrigger(Triggers.youCastThisSpell(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.youCastThisSpell(),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET))
                .addTrigger(Triggers.youCastThisSpell(),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        addStaticEffect(new EntersTappedEffect());
        addStaticEffect(new EnterWithCountersEffect(CounterType.STUN, new Fixed(6)));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER, SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                new DrawCardEffect(1)));
    }
}
