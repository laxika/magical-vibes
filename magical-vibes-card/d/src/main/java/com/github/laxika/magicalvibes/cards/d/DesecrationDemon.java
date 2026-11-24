package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "63")
@CardRegistration(set = "MM3", collectorNumber = "66")
@CardRegistration(set = "DDR", collectorNumber = "44")
public class DesecrationDemon extends Card {

    public DesecrationDemon() {
        // At the beginning of each combat, any opponent may sacrifice a creature of their choice.
        // If a player does, tap this creature and put a +1/+1 counter on it.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT,
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                Subjects.acceptedPlayers()),
                        "Sacrifice a creature? If a player does, it becomes tapped and gets a +1/+1 counter.",
                        SequenceEffect.of(new TapPermanentsEffect(Subjects.source()),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                        null));
    }
}
