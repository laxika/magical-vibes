package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.effect.UnsuspectAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "2")
public class AbsolvingLammasu extends Card {

    public AbsolvingLammasu() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UnsuspectAllCreaturesEffect());

        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                        new GainLifeEffect(3),
                        new SuspectEffect(GrantScope.TARGET)));
    }
}
