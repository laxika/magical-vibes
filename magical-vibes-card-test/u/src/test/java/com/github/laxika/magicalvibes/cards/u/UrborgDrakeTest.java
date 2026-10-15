package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgDrake.class, MetathranZombie.class})
class UrborgDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Urborg Drake can attack throws exception")
    void mustAttackWhenAble() {
        addDrake(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Urborg Drake attacks when declared")
    void attacksWhenDeclared() {
        harness.setLife(player2, 20);
        addDrake(false);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Omitting Urborg Drake while declaring another attacker throws exception")
    void mustBeIncludedAmongAttackers() {
        addDrake(false);
        addCreatureReady(player1, new MetathranZombie());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Urborg Drake does not have to attack while summoning sick")
    void doesNotAttackWithSummoningSickness() {
        Permanent drake = addDrake(true);

        declareAttackers(List.of());

        assertThat(drake.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Urborg Drake does not have to attack while tapped")
    void doesNotAttackWhileTapped() {
        Permanent drake = addDrake(false);
        drake.tap();

        declareAttackers(List.of());

        assertThat(drake.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An untapped Urborg Drake must attack again in a later combat")
    void mustAttackAgainInLaterCombat() {
        Permanent drake = addDrake(false);
        declareAttackers(List.of(0));
        resolveCombat();
        drake.untap();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A nonflying creature cannot block Urborg Drake")
    void nonflyingCreatureCannotBlock() {
        addDrake(false);
        addCreatureReady(player2, new MetathranZombie());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A flying creature can block Urborg Drake")
    void flyingCreatureCanBlock() {
        addDrake(false);
        Permanent blocker = addCreatureReady(player2, new UrborgDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Urborg Drake can block a nonflying attacker")
    void summoningSickDrakeCanBlock() {
        addCreatureReady(player1, new MetathranZombie());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new UrborgDrake());
        blocker.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addDrake(boolean summoningSick) {
        if (!summoningSick) {
            return addCreatureReady(player1, new UrborgDrake());
        }
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new UrborgDrake());
        drake.setSummoningSick(true);
        return drake;
    }
}
