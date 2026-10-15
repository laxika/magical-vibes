package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CourierGriffin.class)
class CourierGriffinTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldGainsTwoLife() {
        harness.setHand(player1, List.of(new CourierGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void lifeIsGainedOnlyWhenTheEnterTriggerResolves() {
        harness.setHand(player1, List.of(new CourierGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Courier Griffin");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsGriffinGainsLifeForItsController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CourierGriffin()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }
}
