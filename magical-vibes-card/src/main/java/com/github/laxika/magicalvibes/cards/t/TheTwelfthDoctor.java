package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "164")
public class TheTwelfthDoctor extends Card {

    public TheTwelfthDoctor() {
        addEffect(EffectSlot.GRANT_DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND,
                new MayEffect(new DemonstrateEffect(), "Copy this spell for an opponent?"));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(null, List.of(new PutCountersOnSourceEffect(1, 1, 1)),
                        (StackEntryPredicate) null, true));
    }
}
