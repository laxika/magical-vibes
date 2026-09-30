package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Frazzle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PetrifiedWoodKin.class, Frazzle.class, Pyromatics.class})
class PetrifiedWoodKinTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst X puts one +1/+1 counter on it for each damage dealt to opponents")
    void bloodthirstCountsDamageToOpponents() {
        gd.recordDamageToPlayer(player2.getId(), 4);

        castPetrifiedWoodKin();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bloodthirst X puts no counters on it when no damage was dealt to opponents")
    void bloodthirstDoesNotApplyWithoutOpponentDamage() {
        castPetrifiedWoodKin();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst X ignores damage dealt to its controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 4);

        castPetrifiedWoodKin();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst X counts damage dealt after the spell was cast but before it resolves")
    void bloodthirstCountsDamageBeforeResolution() {
        PetrifiedWoodKin woodKin = new PetrifiedWoodKin();
        harness.setHand(player1, List.of(woodKin));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from instants prevents an instant from targeting it")
    void protectionFromInstantsPreventsTargeting() {
        PetrifiedWoodKin woodKin = new PetrifiedWoodKin();
        harness.addToBattlefield(player1, woodKin);

        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, woodKin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("The creature spell cannot be countered")
    void cannotBeCountered() {
        PetrifiedWoodKin woodKin = new PetrifiedWoodKin();
        harness.setHand(player1, List.of(woodKin));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.setHand(player2, List.of(new Frazzle()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, woodKin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Petrified Wood-Kin");
        harness.assertInGraveyard(player2, "Frazzle");
    }

    private void castPetrifiedWoodKin() {
        harness.castFromHand(player1, new PetrifiedWoodKin(), "{6}{G}");
        resolveAllTriggers();
    }
}
