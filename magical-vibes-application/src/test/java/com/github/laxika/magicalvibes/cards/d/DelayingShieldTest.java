package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DelayingShieldTest extends BaseCardTest {

    private Permanent shield() {
        harness.addToBattlefield(player1, new DelayingShield());
        return findPermanent(player1, "Delaying Shield");
    }

    private void boltPlayer1() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Damage to the controller becomes delay counters")
    void replacesDamageWithDelayCounters() {
        Permanent shield = shield();
        int lifeBefore = gd.getLife(player1.getId());

        boltPlayer1();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining each upkeep payment causes one life loss per delay counter")
    void decliningPaymentsLosesLifePerCounter() {
        Permanent shield = shield();
        boltPlayer1();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }

    @Test
    @DisplayName("Paying {1}{W} for each counter avoids all upkeep life loss")
    void payingEachCounterAvoidsLifeLoss() {
        Permanent shield = shield();
        boltPlayer1();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The shield only replaces damage to its controller")
    void doesNotReplaceDamageToAnotherPlayer() {
        Permanent shield = shield();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }
}
