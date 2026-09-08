package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "194")
public class CaptainHowlerSeaScourge extends Card {

    public CaptainHowlerSeaScourge() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(new Scaled(new EventValue(), 2), new Fixed(0),
                                new PermanentIsCreaturePredicate()),
                        new RegisterDelayedWatchedCreaturesCombatDamageEffect(List.of(new DrawCardEffect(1)), true, true)
                ));
    }
}
