package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantManGargantuanGenius.class, CrawWurm.class, GrizzlyBears.class})
class GiantManGargantuanGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one green mana for each controlled creature with power 4 or greater")
    void addsManaForControlledCreaturesWithPowerAtLeastFour() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToPrecombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only creatures controlled by Giant-Man's controller")
    void countsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.addToBattlefield(player2, new CrawWurm());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Triggers during its controller's first main phase")
    void triggersDuringControllersFirstMainPhase() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
