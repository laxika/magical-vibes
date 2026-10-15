package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShizukoCallerOfAutumn.class, TrueBeliever.class})
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

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

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
        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Upkeep mana is added only when the triggered ability resolves")
    void manaWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ordinary green mana expires while Shizuko's green mana remains")
    void onlyGeneratedManaPersists() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shizukos controlled by different players both give mana to the active player")
    void bothPlayersShizukosGiveManaToActivePlayer() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());
        harness.addToBattlefield(player2, new ShizukoCallerOfAutumn());

        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent with shroud still receives Shizuko's upkeep mana")
    void opponentWithShroudStillReceivesMana() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());
        harness.addToBattlefield(player2, new TrueBeliever());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A controller with shroud still receives Shizuko's upkeep mana")
    void controllerWithShroudStillReceivesMana() {
        harness.addToBattlefield(player1, new ShizukoCallerOfAutumn());
        harness.addToBattlefield(player1, new TrueBeliever());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }
}
