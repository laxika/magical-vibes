package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AeronautCavalry.class, GrizzlyBears.class, YotianSoldier.class,
        ArtificialEvolution.class, Bitterblossom.class})
class AeronautCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Soldier you control")
    void etbPutsCounterOnAnotherSoldierYouControl() {
        harness.addToBattlefield(player1, new YotianSoldier());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID soldierId = harness.getPermanentId(player1, "Yotian Soldier");
        harness.castCreature(player1, 0, soldierId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Yotian Soldier");
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not target a non-Soldier creature you control")
    void etbDoesNotTargetNonSoldier() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aeronaut Cavalry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItselfWhenNoOtherSoldierExists() {
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aeronaut Cavalry")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsSoldier() {
        harness.addToBattlefield(player2, new AeronautCavalry());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Aeronaut Cavalry")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnotherCopyOfAeronautCavalry() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AeronautCavalry());
        harness.setHand(player1, List.of(new AeronautCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, other.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Aeronaut Cavalry"))
                .filteredOn(permanent -> !permanent.getId().equals(other.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent
                        .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void canPutCounterOnNoncreatureKindredSoldier() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution(), new AeronautCavalry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, soldier.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SOLDIER");

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, soldier.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
