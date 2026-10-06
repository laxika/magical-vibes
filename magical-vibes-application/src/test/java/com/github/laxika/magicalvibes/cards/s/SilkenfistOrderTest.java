package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilkenfistOrder.class, Mossdog.class})
class SilkenfistOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps Silkenfist Order")
    void becomingBlockedUntapsIt() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(order.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple blockers trigger Silkenfist Order only once")
    void multipleBlockersTriggerOnce() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        addCreatureReady(player2, new Mossdog());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Becoming blocked untaps only Silkenfist Order")
    void becomingBlockedUntapsOnlyIt() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        Permanent otherCreature = addCreatureReady(player1, new Mossdog());
        otherCreature.tap();
        Permanent blocker = addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(order.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An unblocked Silkenfist Order does not trigger")
    void unblockedDoesNotTrigger() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(order.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping after becoming blocked keeps Silkenfist Order in combat")
    void untappingKeepsItInCombat() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(order.isTapped()).isFalse();
        assertThat(order.isAttacking()).isTrue();
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Silkenfist Order");
        harness.assertInGraveyard(player2, "Mossdog");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocking with Silkenfist Order does not trigger its ability")
    void blockingDoesNotTrigger() {
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new SilkenfistOrder());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }
}
