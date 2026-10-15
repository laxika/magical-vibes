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
        harness.castFromHand(player1, woodKin, "{6}{G}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());
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

        harness.setHand(player2, List.of(new Frazzle()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castFromHand(player1, woodKin, "{6}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, woodKin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Petrified Wood-Kin");
        harness.assertInGraveyard(player2, "Frazzle");
    }

    @Test
    @DisplayName("Bloodthirst does not count a reduced life total without damage")
    void bloodthirstDoesNotCountLifeLossWithoutDamage() {
        harness.setLife(player2, 10);

        castPetrifiedWoodKin();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst X totals separate damage events rather than life lost")
    void bloodthirstTotalsSeparateDamageEvents() {
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Pyromatics()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        harness.setLife(player2, 20);

        castPetrifiedWoodKin();

        assertThat(findPermanent(player1, "Petrified Wood-Kin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst applies when entering without being cast and uses the entering controller")
    void bloodthirstAppliesToUncastEntryForEitherController() {
        gd.recordDamageToPlayer(player1.getId(), 5);
        gd.recordDamageToPlayer(player2.getId(), 2);

        var permanent = harness.enterBattlefieldAndReturn(player2, new PetrifiedWoodKin());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection also stops instants cast by its controller")
    void protectionStopsControllersInstants() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new PetrifiedWoodKin());
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    private void castPetrifiedWoodKin() {
        harness.castFromHand(player1, new PetrifiedWoodKin(), "{6}{G}");
        resolveAllTriggers();
    }
}
