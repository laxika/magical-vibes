package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EnigmaEidolon;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomirVigSimicVisionary.class, EnigmaEidolon.class, SimicInitiate.class, SimicSignet.class})
class MomirVigSimicVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("A green creature spell offers a creature search to the top of the library")
    void greenCreatureSpellOffersCreatureSearch() {
        addMomir();
        Card searchedCreature = new EnigmaEidolon();
        Card noncreature = new SimicSignet();
        harness.setLibrary(player1, List.of(noncreature, searchedCreature));
        castGreenCreature();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(searchedCreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(searchedCreature, noncreature);
    }

    @Test
    @DisplayName("The green creature trigger may be declined")
    void greenCreatureTriggerMayBeDeclined() {
        addMomir();
        Card top = new SimicSignet();
        Card creature = new EnigmaEidolon();
        harness.setLibrary(player1, List.of(top, creature));
        castGreenCreature();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, creature);
    }

    @Test
    @DisplayName("A blue creature spell puts a revealed creature card into its controller's hand")
    void blueCreatureSpellPutsRevealedCreatureIntoHand() {
        addMomir();
        Card top = new SimicInitiate();
        Card below = new SimicSignet();
        harness.setLibrary(player1, List.of(top, below));
        castBlueCreature();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("A revealed noncreature card stays on top for the blue creature trigger")
    void blueCreatureTriggerLeavesNoncreatureOnTop() {
        addMomir();
        Card top = new SimicSignet();
        Card below = new SimicInitiate();
        harness.setLibrary(player1, List.of(top, below));
        castBlueCreature();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, below);
    }

    @Test
    @DisplayName("A noncreature spell does not trigger either ability")
    void noncreatureSpellDoesNotTriggerEitherAbility() {
        addMomir();
        Card top = new SimicInitiate();
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new SimicSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Momir Vig")
    void opponentsCreatureSpellDoesNotTriggerMomirVig() {
        addMomir();
        Card top = new SimicSignet();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SimicInitiate(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    private void addMomir() {
        harness.addToBattlefield(player1, new MomirVigSimicVisionary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castGreenCreature() {
        harness.castFromHand(player1, new SimicInitiate(), "{G}");
    }

    private void castBlueCreature() {
        harness.castFromHand(player1, new EnigmaEidolon(), "{3}{U}");
    }
}
