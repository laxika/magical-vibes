package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarpetOfFlowersTest extends BaseCardTest {

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

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
