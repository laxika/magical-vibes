package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarpetOfFlowers.class, Island.class})
class CarpetOfFlowersTest extends BaseCardTest {

    @BeforeEach
    void stopAfterMainPhaseInteractions() {
        gd.playerAutoStopSteps.put(player1.getId(),
                Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN));
    }

    @Test
    @DisplayName("Adds mana equal to the target opponent's Islands")
    void addsManaForTargetOpponentsIslands() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        chooseOpponentAndResolve();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The amount is counted when the ability resolves")
    void countsIslandsOnResolution() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player2, new Island());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the first main phase still allows the postcombat trigger")
    void decliningFirstTriggerAllowsPostcombatTrigger() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        chooseOpponentAndDecline();

        advanceToPostcombatMain(player1);
        chooseOpponentAndResolve();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A successful precombat trigger prevents the postcombat trigger")
    void successfulFirstTriggerPreventsPostcombatTrigger() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        chooseOpponentAndResolve();

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Adding zero mana does not prevent the postcombat trigger")
    void addingZeroManaDoesNotPreventPostcombatTrigger() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());

        advanceToPrecombatMain(player1);
        chooseOpponentAndResolveWithoutColorChoice();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        chooseOpponentAndResolveWithoutColorChoice();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller's Islands do not contribute to the mana amount")
    void ignoresControllersIslands() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        chooseOpponentAndResolve();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not trigger during either main phase of the opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A different Carpet can add mana after the first Carpet has done so")
    void tracksManaSeparatelyForEachCarpet() {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        chooseOpponentAndResolve();
        harness.addToBattlefield(player1, new CarpetOfFlowers());

        advanceToPostcombatMain(player1);
        chooseOpponentAndResolve();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Adds the entire amount in any one chosen color")
    void addsManaOfAnyOneColor(ManaColor color) {
        harness.addToBattlefield(player1, new CarpetOfFlowers());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToPrecombatMain(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private void chooseOpponentAndResolve() {
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
    }

    private void chooseOpponentAndDecline() {
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    private void chooseOpponentAndResolveWithoutColorChoice() {
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player, TurnStep.POSTCOMBAT_MAIN);
    }
}
