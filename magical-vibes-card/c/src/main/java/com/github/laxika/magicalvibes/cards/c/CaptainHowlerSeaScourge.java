package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "194")
public class CaptainHowlerSeaScourge extends Card {

    public CaptainHowlerSeaScourge() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(new Scaled(new EventValue(), 2), new Fixed(0),
                                new PermanentIsCreaturePredicate()),
                        new RegisterDelayedWatchedCreaturesCombatDamageEffect(List.of(new DrawCardEffect(1)), true, true)
                ));
    }
}
