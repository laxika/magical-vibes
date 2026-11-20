package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class NassariDeanOfExpression extends Card {

    public NassariDeanOfExpression() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileTopCardMayPlayThisTurnEffect(false)
                .fromLibraryOf(Subjects.eachOpponent()).mayOnlyCast().withAnyManaType());
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
