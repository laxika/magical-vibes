package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.cards.m.MistRaven;
import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotcrownGhoul.class, ThunderousWrath.class, MistRaven.class, DeathWind.class})
class RotcrownGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Dying from zero toughness mills the entire library when fewer than five cards remain")
    void shortLibraryIsMilledIntoGraveyard() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        List<Card> library = List.of(new RotcrownGhoul(), new RotcrownGhoul(), new RotcrownGhoul());
        harness.setLibrary(player1, library);
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castInstant(player1, 0, 3, harness.getPermanentId(player2, "Rotcrown Ghoul"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Rotcrown Ghoul");
        harness.handlePermanentChosen(player2, player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(graveyardSizeBefore + 4)
                .containsAll(library);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Returning Rotcrown Ghoul to hand does not trigger milling")
    void returningToHandDoesNotMill() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new MistRaven()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int player1LibrarySize = gd.playerDecks.get(player1.getId()).size();
        int player2LibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Rotcrown Ghoul"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Rotcrown Ghoul");
        harness.assertNotInGraveyard(player2, "Rotcrown Ghoul");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1LibrarySize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2LibrarySize);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("When Rotcrown Ghoul dies, target player mills five cards")
    void deathMillsTargetPlayerFive() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        UUID ghoulId = harness.getPermanentId(player2, "Rotcrown Ghoul");
        harness.castAndResolveInstant(player1, 0, ghoulId);

        // Ghoul's controller (player2) chooses the target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    @DisplayName("Death trigger can target the Ghoul's own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID ghoulId = harness.getPermanentId(player2, "Rotcrown Ghoul");
        harness.castAndResolveInstant(player1, 0, ghoulId);

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 5);
    }
}
