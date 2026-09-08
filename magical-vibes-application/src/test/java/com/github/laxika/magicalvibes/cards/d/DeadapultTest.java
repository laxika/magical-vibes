package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadapultTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a Zombie and deals 2 damage to a player")
    void sacrificesZombieAndDealsDamageToPlayer() {
        addDeadapultAndZombie();
        harness.setLife(player2, 20);

        activateDeadapult(player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diregraf Ghoul");
        harness.assertInGraveyard(player1, "Diregraf Ghoul");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrifices a Zombie and deals 2 damage to a creature")
    void sacrificesZombieAndDealsDamageToCreature() {
        addDeadapultAndZombie();
        Permanent target = new Permanent(new LlanowarElves());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);
        UUID targetId = target.getId();

        activateDeadapult(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diregraf Ghoul");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Zombie")
    void cannotActivateWithoutZombie() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a Zombie");
    }

    private void addDeadapultAndZombie() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void activateDeadapult(UUID targetId) {
        harness.activateAbility(player1, 0, null, targetId);
    }
}
