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

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

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

    @Test
    @DisplayName("The upkeep ability uses the stack and resolves after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Springcaller adds its own persistent mana")
    void multipleCopiesAddManaIndependently() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Only the generated mana survives, even when other mana is also green")
    void ordinaryGreenManaDoesNotPersist() {
        harness.addToBattlefield(player1, new SakuraTribeSpringcaller());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
