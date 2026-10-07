package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalkingTiger.class, ShuFootSoldiers.class})
class StalkingTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Stalking Tiger can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        addCreatureReady(player1, new StalkingTiger());

        addCreatureReady(player2, new StalkingTiger());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Stalking Tiger deals combat damage when unblocked")
    void dealsCombatDamageWhenUnblocked() {
        addCreatureReady(player1, new StalkingTiger());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Stalking Tiger cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new StalkingTiger());

        addCreatureReady(player2, new StalkingTiger());
        addCreatureReady(player2, new StalkingTiger());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Stalking Tiger can remain unblocked")
    void canRemainUnblocked() {
        addCreatureReady(player1, new StalkingTiger());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each Stalking Tiger can be blocked by one creature")
    void eachAttackerCanBeBlockedByOneCreature() {
        addCreatureReady(player1, new StalkingTiger());
        addCreatureReady(player1, new StalkingTiger());

        Permanent firstBlocker = addCreatureReady(player2, new StalkingTiger());
        Permanent secondBlocker = addCreatureReady(player2, new StalkingTiger());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stalking Tiger's restriction does not affect other creatures")
    void restrictionOnlyAppliesToStalkingTiger() {
        addCreatureReady(player1, new ShuFootSoldiers());
        addCreatureReady(player1, new StalkingTiger());

        Permanent firstBlocker = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent secondBlocker = addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stalking Tiger can block another creature alongside a second blocker")
    void canBlockAlongsideAnotherCreature() {
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent tiger = addCreatureReady(player2, new StalkingTiger());
        Permanent otherBlocker = addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(tiger.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An attacking Stalking Tiger does not limit blockers of another attacker")
    void otherAttackerCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new StalkingTiger());
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent firstBlocker = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent secondBlocker = addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
