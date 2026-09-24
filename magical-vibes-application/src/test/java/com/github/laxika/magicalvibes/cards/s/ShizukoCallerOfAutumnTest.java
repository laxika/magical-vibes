package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShizukoCallerOfAutumn.class)
class ShizukoCallerOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("The active player adds three green mana during each upkeep")
    void activePlayerAddsManaDuringEachUpkeep() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("The generated green mana survives a step transition")
    void generatedManaSurvivesStepTransition() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player2.getId());
        pool.add(ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The controller also adds three green mana during their own upkeep")
    void controllerAddsManaDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The generated green mana expires at the end of the turn")
    void generatedManaExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player2.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }
}
