package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrazedGoblin.class})
class CrazedGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Crazed Goblin must attack each combat if able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new CrazedGoblin());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Crazed Goblin attacks when declared")
    void attacksWhenDeclared() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CrazedGoblin());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Crazed Goblin does not need to attack while summoning sick")
    void doesNotHaveToAttackWithSummoningSickness() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());

        declareAttackers(List.of());

        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Crazed Goblin does not need to attack while tapped")
    void doesNotHaveToAttackWhenTapped() {
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        goblin.tap();

        declareAttackers(List.of());

        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Every able Crazed Goblin must attack, not just one")
    void everyAbleGoblinMustAttack() {
        addCreatureReady(player1, new CrazedGoblin());
        addCreatureReady(player1, new CrazedGoblin());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0, 1));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An untapped Crazed Goblin must attack again in another combat")
    void mustAttackAgainInAnotherCombat() {
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        declareAttackers(List.of(0));
        goblin.untap();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's Crazed Goblin does not require the active player to attack")
    void opponentsGoblinDoesNotForceActivePlayerToAttack() {
        Permanent goblin = addCreatureReady(player2, new CrazedGoblin());

        declareAttackers(List.of());

        assertThat(goblin.isAttacking()).isFalse();
        harness.assertLife(player1, 20);
    }
}
