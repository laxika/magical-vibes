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
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "114")
@CardRegistration(set = "WHO", collectorNumber = "403")
@CardRegistration(set = "WHO", collectorNumber = "719")
@CardRegistration(set = "WHO", collectorNumber = "994")
public class TheBeastDeathlessPrince extends Card {

    public TheBeastDeathlessPrince() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.STUN, new Fixed(6)));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER,
                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER, new DrawCardEffect(1));
    }
}
