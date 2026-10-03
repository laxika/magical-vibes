package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NornsChoirmaster.class, GrizzlyBears.class})
class NornsChoirmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferates when a commander enters the battlefield")
    void proliferatesWhenCommanderEnters() {
        addCreatureReady(player1, new NornsChoirmaster());
        Permanent bearWithCounter = addCreatureReady(player1, new GrizzlyBears());
        bearWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bearWithCounter.getId()));

        assertThat(bearWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferates when a commander attacks")
    void proliferatesWhenCommanderAttacks() {
        addCreatureReady(player1, new NornsChoirmaster());
        Permanent bearWithCounter = addCreatureReady(player1, new GrizzlyBears());
        bearWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.makeCommander(player1.getId(), bearWithCounter.getCard());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bearWithCounter.getId()));

        assertThat(bearWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a noncommander attacker")
    void doesNotTriggerForNoncommander() {
        addCreatureReady(player1, new NornsChoirmaster());
        Permanent bearWithCounter = addCreatureReady(player1, new GrizzlyBears());
        bearWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bearWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
