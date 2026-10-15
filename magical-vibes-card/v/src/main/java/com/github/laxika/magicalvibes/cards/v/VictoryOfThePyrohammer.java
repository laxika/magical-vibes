package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YWOE", collectorNumber = "12")
public class VictoryOfThePyrohammer extends Card {

    public VictoryOfThePyrohammer() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, SequenceEffect.of(
                new MassDamageEffect(4, false, false, true, null),
                new GrantStaticEffectToSourceEffect(
                        new GrantEffectEffect(
                                new DamageNotRemovedDuringCleanupEffect(),
                                GrantScope.ALL_CREATURES_INCLUDING_SELF))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MassDamageEffect(1, false, false, true, null));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MassDamageEffect(1, false, false, true, null));
    }
}
