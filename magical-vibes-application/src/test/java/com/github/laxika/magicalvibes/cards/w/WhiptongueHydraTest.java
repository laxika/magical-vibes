package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeathWard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiptongueHydra.class, SerraAngel.class, GrizzlyBears.class, DeathWard.class, Jump.class, Unsummon.class})
class WhiptongueHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with flying and gets a counter for each one")
    void destroysFlyingCreaturesAndGetsCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.castFromHand(player1, new WhiptongueHydra(), "{5}{G}");
        resolveAllTriggers();

        Permanent hydra = findPermanent(player1, "Whiptongue Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not destroy creatures without flying")
    void doesNotDestroyNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WhiptongueHydra(), "{5}{G}");
        resolveAllTriggers();

        Permanent hydra = findPermanent(player1, "Whiptongue Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Regenerated flying creatures survive and do not contribute counters")
    void regeneratedFlyingCreatureDoesNotContributeCounter() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent protectedAngel = findPermanent(player1, "Serra Angel");

        harness.castFromHand(player1, new WhiptongueHydra(), "{5}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, protectedAngel.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(protectedAngel.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(findPermanent(player1, "Whiptongue Hydra")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Checks flying at resolution, including flying granted to Hydra itself")
    void destroysHydraIfItGainsFlyingBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.castFromHand(player1, new WhiptongueHydra(), "{5}{G}");
        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Whiptongue Hydra");

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, hydra.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Whiptongue Hydra");
        harness.assertInGraveyard(player1, "Whiptongue Hydra");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Still destroys flying creatures when Hydra leaves before its trigger resolves")
    void triggerResolvesAfterHydraLeavesBattlefield() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.castFromHand(player1, new WhiptongueHydra(), "{5}{G}");
        harness.passBothPriorities();
        Permanent hydra = findPermanent(player1, "Whiptongue Hydra");

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, hydra.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Whiptongue Hydra");
        harness.assertInHand(player1, "Whiptongue Hydra");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
