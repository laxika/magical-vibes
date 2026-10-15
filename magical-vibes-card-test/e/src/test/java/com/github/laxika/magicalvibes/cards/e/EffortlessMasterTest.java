package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EffortlessMaster.class, LightningBolt.class})
class EffortlessMasterTest extends BaseCardTest {

    @Test
    void countsItselfAsTheSecondSpellThisTurn() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        castEffortlessMaster();

        assertThat(findPermanent(player1, "Effortless Master").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotEnterWithCountersAsTheFirstSpellThisTurn() {
        castEffortlessMaster();

        assertThat(findPermanent(player1, "Effortless Master").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithCountersAsTheThirdSpellThisTurn() {
        castEffortlessMaster();
        castEffortlessMaster();
        castEffortlessMaster();

        assertThat(findPermanents(player1, "Effortless Master"))
                .extracting(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactly(0, 2, 2);
    }

    @Test
    void doesNotCountSpellsCastByTheOpponent() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new EffortlessMaster(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.castFromHand(player2, new EffortlessMaster(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);

        castEffortlessMaster();

        assertThat(findPermanent(player1, "Effortless Master").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void checksTheSpellCountOnEnteringRatherThanOnCasting() {
        harness.castFromHand(player1, new EffortlessMaster(), "{2}{U}{R}");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Effortless Master")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Effortless Master").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void vigilanceLeavesItUntappedWhenAttacking() {
        var master = addCreatureReady(player1, new EffortlessMaster());
        addCreatureReady(player2, new EffortlessMaster());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(master.isAttacking()).isTrue();
        assertThat(master.isTapped()).isFalse();
    }

    @Test
    void menaceRejectsASingleBlocker() {
        addCreatureReady(player1, new EffortlessMaster());
        addCreatureReady(player2, new EffortlessMaster());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new EffortlessMaster());
        addCreatureReady(player2, new EffortlessMaster());
        addCreatureReady(player2, new EffortlessMaster());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    private void castEffortlessMaster() {
        harness.castFromHand(player1, new EffortlessMaster(), "{2}{U}{R}");
        harness.passBothPriorities();
    }
}
