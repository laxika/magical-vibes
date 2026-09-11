package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TurntimberAscetic.class)
class TurntimberAsceticTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldAndGainsThreeLife() {
        harness.setLife(player1, 10);
        castTurntimberAscetic();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player1, "Turntimber Ascetic");
    }

    @Test
    void etbTriggerOnlyGainsLifeForItsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        castTurntimberAscetic();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    private void castTurntimberAscetic() {
        harness.setHand(player1, List.of(new TurntimberAscetic()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
