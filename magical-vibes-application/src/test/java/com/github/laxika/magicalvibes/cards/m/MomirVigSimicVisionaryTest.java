package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomirVigSimicVisionary.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class})
class MomirVigSimicVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("A green creature spell offers a creature search to the top of the library")
    void greenCreatureSpellOffersCreatureSearch() {
        addMomir();
        Card searchedCreature = new FugitiveWizard();
        Card noncreature = new Forest();
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
        Card top = new Forest();
        Card creature = new FugitiveWizard();
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
        Card top = new GrizzlyBears();
        Card below = new Forest();
        harness.setLibrary(player1, List.of(top, below));
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("A revealed noncreature card stays on top for the blue creature trigger")
    void blueCreatureTriggerLeavesNoncreatureOnTop() {
        addMomir();
        Card top = new Forest();
        Card below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, below));
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, below);
    }

    private void addMomir() {
        harness.addToBattlefield(player1, new MomirVigSimicVisionary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castGreenCreature() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
