package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AshlingThePilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("First two resolutions only add +1/+1 counters")
    void firstTwoResolutionsAddCounters() {
        Permanent ashling = addAshling(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);

        activateAndResolve();
        activateAndResolve();

        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Third resolution removes all counters and deals that much damage to each creature and player")
    void thirdResolutionExplodes() {
        addAshling(player1);
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 6);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        // 3 +1/+1 counters removed → 3 damage to each creature and each player.
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears"); // 2/2 dies to 3 damage
        harness.assertInGraveyard(player1, "Ashling the Pilgrim"); // 1/1 (counters removed) dies to its own blast
    }

    @Test
    @DisplayName("Bonus fires only on the exact third resolution, not on later ones")
    void bonusOnlyOnThirdResolution() {
        Permanent ashling = addAshling(player1);
        ashling.setToughnessModifier(30); // survive its own blast so we can keep activating
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 12);

        for (int i = 0; i < 6; i++) {
            activateAndResolve();
        }

        // Only the third resolution dealt damage; the 4th–6th just re-accumulate counters.
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(ashling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAshling(Player player) {
        Permanent perm = new Permanent(new AshlingThePilgrim());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
