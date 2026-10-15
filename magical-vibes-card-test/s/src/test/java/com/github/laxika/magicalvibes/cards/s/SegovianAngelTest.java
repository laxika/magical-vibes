package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.w.WebweaverChangeling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SegovianAngel.class, MotherBear.class, WebweaverChangeling.class})
class SegovianAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Segovian Angel")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SegovianAngel());
        addCreatureReady(player2, new MotherBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A flying creature can block Segovian Angel")
    void flyingCreatureCanBlockSegovianAngel() {
        addCreatureReady(player1, new SegovianAngel());
        Permanent blocker = addCreatureReady(player2, new SegovianAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Segovian Angel untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent angel = addCreatureReady(player1, new SegovianAngel());

        declareAttackers(List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature with reach can block Segovian Angel")
    void reachCreatureCanBlockSegovianAngel() {
        addCreatureReady(player1, new SegovianAngel());
        Permanent blocker = addCreatureReady(player2, new WebweaverChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Segovian Angel to attack")
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent angel = addCreatureReady(player1, new SegovianAngel());
        angel.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Segovian Angel to attack")
    void vigilanceDoesNotBypassSummoningSickness() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SegovianAngel());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(angel.isAttacking()).isFalse();
    }
}
