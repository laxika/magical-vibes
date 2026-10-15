package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevoutMonk.class})
class DevoutMonkTest extends BaseCardTest {

    @Test
    void etbGainsOneLife() {
        harness.setHand(player1, List.of(new DevoutMonk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devout Monk");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifeGainWaitsForEnterTriggerToResolve() {
        harness.setHand(player1, List.of(new DevoutMonk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devout Monk");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachMonkGainsLifeOnlyForItsOwnEntry() {
        harness.setHand(player1, List.of(new DevoutMonk(), new DevoutMonk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
