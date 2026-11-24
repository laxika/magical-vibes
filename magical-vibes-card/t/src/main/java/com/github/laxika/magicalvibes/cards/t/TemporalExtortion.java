package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "81")
@CardRegistration(set = "SLD", collectorNumber = "1859")
public class TemporalExtortion extends Card {

    public TemporalExtortion() {
        addTrigger(Triggers.youCastThisSpell(), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                EachPlayerPaysAnyManaEffect.lifeExactly(Subjects.acceptedPlayers(), new HalfControllerLifeRoundedUp()),
                "Pay half your life, rounded up? If a player does, Temporal Extortion is countered.",
                CounterSpellEffect.triggeringSpell(), null).payingAsEachAccepts());
        addSpellEffect(new ControllerExtraTurnEffect(1));
    }
}
