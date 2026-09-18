package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WurmskinForger.class, YotianSoldier.class, WeldingJar.class})
class WurmskinForgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can put all three counters on one target creature")
    void distributesAllCountersToOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        gd.pendingETBDamageAssignments = Map.of(target.getId(), 3);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB distributes three +1/+1 counters among two target creatures")
    void distributesCountersAmongTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can distribute one counter to each of three target creatures")
    void distributesCountersAmongThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        gd.pendingETBDamageAssignments = Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB ignores a noncreature assignment at resolution")
    void ignoresNoncreatureAssignment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2, artifact.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB skips a target that left the battlefield")
    void skipsTargetThatLeftBattlefield() {
        Permanent staying = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        gd.pendingETBDamageAssignments = Map.of(staying.getId(), 2, leaving.getId(), 1);

        castWurmskinForger();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(leaving.getId()));
        harness.passBothPriorities();

        assertThat(staying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB offers one to three creature targets and excludes noncreatures")
    void offersCreatureTargetsWhenTriggerIsPutOnTheStack() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        gd.pendingETBDamageAssignments = Map.of(ownCreature.getId(), 3);

        castWurmskinForger();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .contains(ownCreature.getId(), opponentCreature.getId())
                .doesNotContain(noncreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void castWurmskinForger() {
        harness.castFromHand(player1, new WurmskinForger(), "{5}{G}{G}");
    }
}
