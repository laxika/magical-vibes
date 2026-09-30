package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiSlayer.class, Squire.class})
class DauthiSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Dauthi Slayer can attack throws exception")
    void mustAttackWhenAble() {
        addSlayer();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Dauthi Slayer while declaring other attackers throws exception")
    void mustBeIncludedAmongAttackers() {
        addSlayer();

        addCreatureReady(player1, new Squire());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Dauthi Slayer attacks for 2 when declared")
    void attacksForTwo() {
        harness.setLife(player2, 20);
        addSlayer();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Dauthi Slayer does not have to attack while summoning sick")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DauthiSlayer());
        addCreatureReady(player1, new Squire());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Shadow stops a creature without shadow from blocking Dauthi Slayer")
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player2, new Squire());

        addCreatureReady(player1, new DauthiSlayer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow allows a creature with shadow to block Dauthi Slayer")
    void canBeBlockedByCreatureWithShadow() {
        Permanent blocker = addCreatureReady(player2, new DauthiSlayer());
        addCreatureReady(player1, new DauthiSlayer());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shadow stops Dauthi Slayer from blocking a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        Permanent blocker = addCreatureReady(player2, new DauthiSlayer());
        addCreatureReady(player1, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    private void addSlayer() {
        addCreatureReady(player1, new DauthiSlayer());
    }
}
