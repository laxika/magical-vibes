package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleAcolyte.class})
class TempleAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters the battlefield")
    void gainsThreeLifeOnEnter() {
        harness.setHand(player1, List.of(new TempleAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setHand(player1, List.of(new TempleAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Temple Acolyte");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast gives life to its controller")
    void gainsLifeForOtherControllerWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player2, new TempleAcolyte());

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Temple Acolyte");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
}
