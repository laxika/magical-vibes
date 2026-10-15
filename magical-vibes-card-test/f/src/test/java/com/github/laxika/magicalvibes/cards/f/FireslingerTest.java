package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fireslinger.class})
class FireslingerTest extends BaseCardTest {

    @Test
    @DisplayName("Targeting its controller deals both points of damage to that player")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Fireslinger());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can target itself and still deals damage to its controller")
    void canTargetItself() {
        harness.setLife(player1, 20);
        Permanent fireslinger = addCreatureReady(player1, new Fireslinger());

        harness.activateAbility(player1, 0, null, fireslinger.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fireslinger");
        harness.assertNotOnBattlefield(player1, "Fireslinger");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Ability resolves after its source is killed in response")
    void resolvesAfterSourceDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent fireslinger = addCreatureReady(player1, new Fireslinger());
        addCreatureReady(player2, new Fireslinger());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, fireslinger.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fireslinger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Fireslinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new Fireslinger());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 1 damage to target player and 1 damage to its controller")
    void damagesTargetPlayerAndController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent fireslinger = addCreatureReady(player1, new Fireslinger());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 19);
        assertThat(fireslinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, killing a 1/1, and 1 to its controller")
    void damagesTargetCreatureAndController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Fireslinger());
        addCreatureReady(player2, new Fireslinger());

        UUID targetId = harness.getPermanentId(player2, "Fireslinger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fireslinger");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not deal damage if its target leaves before resolution")
    void doesNotResolveWhenTargetLeavesBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Fireslinger());
        Permanent target = addCreatureReady(player2, new Fireslinger());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
