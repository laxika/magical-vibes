package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakuraTribeSpringcaller.class})
class SakuraTribeSpringcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one green mana during its controller's upkeep")
    void addsGreenManaDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The generated green mana survives a step transition")
    void generatedManaSurvivesStepTransition() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The generated green mana expires at the end of the turn")
    void generatedManaExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);

        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
