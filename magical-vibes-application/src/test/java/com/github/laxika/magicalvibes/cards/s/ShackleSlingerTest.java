package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShackleSlinger.class, Shock.class, GrizzlyBears.class})
class ShackleSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell taps an untapped creature an opponent controls")
    void secondSpellTapsUntappedCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("The second spell puts a stun counter on a tapped creature an opponent controls")
    void secondSpellStunsTappedCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.tap();
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    private void castTwoShocks() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
    }
}
