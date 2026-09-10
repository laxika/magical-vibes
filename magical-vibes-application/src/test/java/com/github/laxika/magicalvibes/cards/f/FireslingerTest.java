package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Fireslinger.class)
class FireslingerTest extends BaseCardTest {

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
