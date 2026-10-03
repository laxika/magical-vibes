package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodHost.class, RuneclawBear.class, FleshToDust.class})
class BloodHostTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Blood Host and gains 2 life")
    void sacrificingAnotherCreaturePutsCounterAndGainsLife() {
        Permanent bloodHost = harness.addToBattlefieldAndReturn(player1, new BloodHost());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bloodHost.getEffectivePower()).isEqualTo(4);
        assertThat(bloodHost.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Blood Host cannot sacrifice itself")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new BloodHost());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood Host");
    }

    @Test
    void tappedSummoningSickHostCanActivateAndSacrificeIsPaidBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BloodHost());
        host.setSummoningSick(true);
        host.setTapped(true);
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertLife(player1, lifeBefore);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(host.isTapped()).isTrue();
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new BloodHost());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new BloodHost());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void gainsLifeEvenIfHostIsDestroyedInResponse() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BloodHost());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Blood Host");
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
