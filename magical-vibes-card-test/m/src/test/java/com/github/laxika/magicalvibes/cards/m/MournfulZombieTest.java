package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MournfulZombie.class})
class MournfulZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 1 life and the Zombie becomes tapped")
    void targetPlayerGainsLife() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 1);
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller can be the target")
    void controllerCanBeTargeted() {
        addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Requires white mana")
    void requiresWhiteMana() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zombie.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        zombie.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Requires a player target")
    void requiresPlayerTarget() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zombie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(zombie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(zombie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability uses the stack and resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(zombie.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        gd.playerBattlefields.get(player1.getId()).remove(zombie);
        gd.playerGraveyards.get(player1.getId()).add(zombie.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.stack).isEmpty();
    }
}
