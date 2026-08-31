package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdOfRot.class, ZombieGoliath.class, Mountain.class})
class ShepherdOfRotTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses life for every Zombie on the battlefield")
    void eachPlayerLosesLifeForZombiesOnBattlefield() {
        Permanent shepherd = addCreatureReady(player1, new ShepherdOfRot());
        harness.addToBattlefield(player1, new ZombieGoliath());
        harness.addToBattlefield(player2, new ZombieGoliath());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(shepherd.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counts Zombies when the ability resolves")
    void countsZombiesAtResolution() {
        addCreatureReady(player1, new ShepherdOfRot());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new ZombieGoliath());
        harness.addToBattlefield(player1, new ZombieGoliath());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player2.getId()).remove(zombie);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
