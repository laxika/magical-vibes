package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuseSeeker.class, Island.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class MuseSeekerTest extends BaseCardTest {

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting a cheap instant draws a card then requires a discard")
    void cheapSpellDrawsThenDiscards() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Opus trigger — draws, then awaits discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew a card, not yet discarded

        harness.handleCardChosen(player1, 0);

        // Drew one (deck -1) and discarded one (into graveyard); hand ends empty.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Casting a five-mana spell draws a card with no discard")
    void fiveManaSpellDrawsOnly() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities(); // resolve Opus trigger — draws only

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew and kept
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Four mana spent still requires discarding even with extra mana available")
    void fourManaSpellRequiresDiscard() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player1);
        harness.setHand(player1, List.of(new Hurricane(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Muse Seeker")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The draw and discard still resolve after Muse Seeker is destroyed")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new MuseSeeker());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        setUpMainPhase(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Muse Seeker"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Muse Seeker");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
    }
}
