package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WellOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life at your end step when you control no untapped lands")
    void gainsLifeWithNoUntappedLands() {
        harness.addToBattlefield(player1, new WellOfLife());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Does not gain life when you control an untapped land")
    void doesNotGainLifeWithUntappedLand() {
        harness.addToBattlefield(player1, new WellOfLife());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Gains life when all lands you control are tapped")
    void gainsLifeWithOnlyTappedLands() {
        harness.addToBattlefield(player1, new WellOfLife());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLife(player1, 10);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The condition is checked again when the ability resolves")
    void doesNotGainLifeIfLandBecomesUntappedBeforeResolution() {
        harness.addToBattlefield(player1, new WellOfLife());
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
