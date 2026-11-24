package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "62")
public class StromgaldSpy extends Card {

    public StromgaldSpy() {
        // Whenever this creature attacks and isn't blocked, you may have defending player play with
        // their hand revealed for as long as this creature remains on the battlefield.
        // If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(SequenceEffect.of(
                        new GrantStaticEffectToPlayerEffect(new PlayWithHandsRevealedEffect(Subjects.you()),
                                Subjects.defendingPlayer(), Duration.WHILE_SOURCE_REMAINS),
                        new AssignNoCombatDamageEffect()),
                        "You may have defending player play with their hand revealed for as long as this creature remains on the battlefield. If you do, this creature assigns no combat damage this turn."));
    }
}
