package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MakeDefendingPlayerPlayWithHandRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ALL", collectorNumber = "62")
public class StromgaldSpy extends Card {

    public StromgaldSpy() {
        // Whenever this creature attacks and isn't blocked, you may have defending player play with
        // their hand revealed for as long as this creature remains on the battlefield.
        // If you do, this creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new MakeDefendingPlayerPlayWithHandRevealedEffect(),
                        new AssignNoCombatDamageEffect()),
                        "You may have defending player play with their hand revealed for as long as this creature remains on the battlefield. If you do, this creature assigns no combat damage this turn."));
    }
}
