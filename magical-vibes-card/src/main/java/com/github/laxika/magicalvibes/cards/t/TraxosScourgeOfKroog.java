package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "234")
public class TraxosScourgeOfKroog extends Card {

    public TraxosScourgeOfKroog() {
        // Traxos enters tapped and doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());

        // Whenever you cast a historic spell, untap Traxos.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new UntapSelfEffect())
        ));
    }
}
