package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberBeast.class, WoodlandDruid.class, TurnToFrog.class})
class EmberBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Two Ember Beasts can attack together")
    void twoBeastsCanAttackTogether() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmberBeast());
        addCreatureReady(player1, new EmberBeast());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("An idle creature does not allow Ember Beast to attack alone")
    void idleCreatureDoesNotAllowAttackingAlone() {
        addCreatureReady(player1, new EmberBeast());
        addCreatureReady(player1, new EmberBeast());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Two Ember Beasts can block the same attacker together")
    void twoBeastsCanBlockSameAttacker() {
        addCreatureReady(player1, new WoodlandDruid());
        Permanent first = addCreatureReady(player2, new EmberBeast());
        Permanent second = addCreatureReady(player2, new EmberBeast());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareBlockers(gd, player2, List.of(
                        new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An idle creature does not allow Ember Beast to block alone")
    void idleCreatureDoesNotAllowBlockingAlone() {
        addCreatureReady(player1, new WoodlandDruid());
        addCreatureReady(player2, new EmberBeast());
        addCreatureReady(player2, new EmberBeast());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block alone");
    }

    @Test
    @DisplayName("Ember Beast can attack alone after losing its abilities")
    void canAttackAloneAfterLosingAbilities() {
        harness.setLife(player2, 20);
        Permanent beast = addCreatureReady(player1, new EmberBeast());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, beast.getId());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ember Beast can block alone after losing its abilities")
    void canBlockAloneAfterLosingAbilities() {
        addCreatureReady(player1, new WoodlandDruid());
        Permanent beast = addCreatureReady(player2, new EmberBeast());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, beast.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(beast.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ember Beast can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new EmberBeast());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ember Beast can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new EmberBeast());
        addCreatureReady(player1, new WoodlandDruid());

        declareAttackers(List.of(0, 1));

        // Ember Beast (3/4) + Woodland Druid (1/2) = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ember Beast can't block alone")
    void cantBlockAlone() {
        addCreatureReady(player1, new WoodlandDruid());

        addCreatureReady(player2, new EmberBeast());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ember Beast can block with another creature")
    void canBlockWithAnother() {
        addCreatureReady(player1, new WoodlandDruid());

        addCreatureReady(player1, new WoodlandDruid());

        Permanent beast = addCreatureReady(player2, new EmberBeast());

        Permanent druid = addCreatureReady(player2, new WoodlandDruid());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(beast.isBlocking()).isTrue();
        assertThat(druid.isBlocking()).isTrue();
    }
}
