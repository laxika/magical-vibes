package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StatuteOfDenial.class, Forest.class, FugitiveWizard.class, RuneclawBear.class, LightningStrike.class})
class StatuteOfDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and does not draw without a blue creature")
    void countersWithoutBlueCreatureNoDraw() {
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new StatuteOfDenial()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counters a spell, draws, and then discards with a blue creature")
    void countersAndLootsWithBlueCreature() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new StatuteOfDenial(), new RuneclawBear()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's blue creature and your green creature do not enable looting")
    void opponentsBlueCreatureDoesNotEnableLooting() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new RuneclawBear());
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new StatuteOfDenial()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The card drawn can be chosen for the mandatory discard")
    void canDiscardTheDrawnCard() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new StatuteOfDenial(), new RuneclawBear()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A blue creature spell on the stack does not enable looting")
    void blueCreatureSpellIsNotABlueCreatureYouControl() {
        FugitiveWizard wizard = new FugitiveWizard();
        harness.setHand(player1, List.of(wizard, new StatuteOfDenial()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, wizard.getId());

        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Statute of Denial");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Losing the blue creature in response prevents looting but still counters")
    void conditionIsCheckedAtResolution() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears, new LightningStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new StatuteOfDenial()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Fugitive Wizard"));
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Statute of Denial");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
