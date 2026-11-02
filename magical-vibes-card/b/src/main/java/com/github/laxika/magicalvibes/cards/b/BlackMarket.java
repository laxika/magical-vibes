package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "116")
@CardRegistration(set = "SLD", collectorNumber = "1577")
@CardRegistration(set = "SLD", collectorNumber = "2485")
@CardRegistration(set = "MB1", collectorNumber = "98")
@CardRegistration(set = "C15", collectorNumber = "116")
@CardRegistration(set = "PIP", collectorNumber = "183")
@CardRegistration(set = "PIP", collectorNumber = "463")
@CardRegistration(set = "PIP", collectorNumber = "711")
@CardRegistration(set = "PIP", collectorNumber = "991")
@CardRegistration(set = "C17", collectorNumber = "98")
public class BlackMarket extends Card {

    public BlackMarket() {
        // Whenever a creature dies, put a charge counter on this enchantment.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new PutCountersOnSourceCardEffect(CounterType.CHARGE));

        // At the beginning of your first main phase, add {B} for each charge counter on this enchantment.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardManaEffect(ManaColor.BLACK, new CountersOnSource(CounterType.CHARGE)));
    }
}
