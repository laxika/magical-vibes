package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the cumulative upkeep draws a card and keeps Psychic Vortex")
    void payingUpkeepDrawsACard() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new PsychicVortex());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(vortex.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vortex);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("The second upkeep draws two cards")
    void secondUpkeepDrawsTwoCards() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new PsychicVortex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int handAfterFirst = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(vortex.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vortex);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterFirst + 2);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Psychic Vortex")
    void decliningSacrificesVortex() {
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new PsychicVortex());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vortex);
        harness.assertInGraveyard(player1, "Psychic Vortex");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("At the controller's end step, a land is sacrificed and the hand is discarded")
    void endStepSacrificesLandAndDiscardsHand() {
        harness.addToBattlefield(player1, new PsychicVortex());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Island")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .contains("Island", "Grizzly Bears");
    }

    @Test
    @DisplayName("Controlling two lands makes the end-step sacrifice a choice")
    void endStepChoosesWhichLandToSacrifice() {
        harness.addToBattlefield(player1, new PsychicVortex());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(countPermanents(player1, "Island") + countPermanents(player1, "Forest")).isEqualTo(2);
    }

    @Test
    @DisplayName("The end-step trigger still empties the hand when no land is controlled")
    void endStepWithNoLandStillDiscards() {
        harness.addToBattlefield(player1, new PsychicVortex());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
