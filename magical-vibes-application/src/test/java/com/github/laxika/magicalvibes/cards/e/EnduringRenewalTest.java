package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnduringRenewalTest extends BaseCardTest {

    // ===== Hand revealed =====

    @Test
    @DisplayName("Opponent sees the controller's hand; controller does not see the opponent's")
    void onlyControllerHandRevealed() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p1Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));
    }

    // ===== Draw replacement =====

    @Test
    @DisplayName("Revealed creature card goes to the graveyard instead of being drawn")
    void revealedCreatureGoesToGraveyard() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Revealed non-creature is drawn normally")
    void revealedNonCreatureIsDrawn() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Plains(), new Forest())));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        harness.assertInHand(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Empty library reveal does not lose the game")
    void emptyLibraryDoesNotLose() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setLibrary(player1, new ArrayList<>());

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gameLogContains("reveals no cards")).isTrue();
    }

    @Test
    @DisplayName("Creature milled by the draw replacement is not returned to hand")
    void milledCreatureNotReturnedToHand() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Death return =====

    @Test
    @DisplayName("A creature put into the graveyard from the battlefield returns to hand")
    void creatureDiesReturnsToHand() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Return fizzles if the creature leaves the graveyard in response")
    void returnFizzlesIfRemovedFromGraveyard() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);

        assertThat(gd.stack).isNotEmpty();
        Card dead = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        gd.playerGraveyards.get(player1.getId()).remove(dead);
        gd.addToExile(player1.getId(), dead);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
