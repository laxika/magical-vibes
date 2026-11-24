package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "68")
public class DashHopes extends Card {

    public DashHopes() {
        addTrigger(Triggers.youCastThisSpell(), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                EachPlayerPaysAnyManaEffect.lifeExactly(Subjects.acceptedPlayers(), new Fixed(5)),
                "Pay 5 life? If a player does, Dash Hopes is countered.",
                CounterSpellEffect.triggeringSpell(), null).payingAsEachAccepts());
        addSpellEffect(new CounterSpellEffect());
    }
}
