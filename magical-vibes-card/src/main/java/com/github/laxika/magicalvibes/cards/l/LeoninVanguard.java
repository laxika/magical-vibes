package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M19", collectorNumber = "22")
public class LeoninVanguard extends Card {

    public LeoninVanguard() {
        // At the beginning of combat on your turn, if you control three or more creatures,
        // this creature gets +1/+1 until end of turn and you gain 1 life.
        // Intervening-if (CR 603.4): gated at trigger time and re-checked on resolution.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                SequenceEffect.of(new BoostSelfEffect(1, 1), new GainLifeEffect(1))
        ));
    }
}
