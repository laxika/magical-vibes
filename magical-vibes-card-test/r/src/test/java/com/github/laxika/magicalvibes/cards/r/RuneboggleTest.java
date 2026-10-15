package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Runeboggle.class, GhostWarden.class})
class RuneboggleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay and draws a card")
    void countersSpellWhenControllerCannotPayAndDraws() {
        harness.setLibrary(player2, List.of(new GhostWarden()));

        GhostWarden warden = new GhostWarden();
        harness.setHand(player1, List.of(warden));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new Runeboggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warden.getId());

        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertInGraveyard(player2, "Runeboggle");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden");
    }

    @Test
    @DisplayName("Counters a spell when its controller declines to pay and draws a card")
    void countersSpellWhenControllerDeclinesToPayAndDraws() {
        harness.setLibrary(player2, List.of(new GhostWarden()));

        GhostWarden warden = new GhostWarden();
        harness.setHand(player1, List.of(warden));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new Runeboggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warden.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertInGraveyard(player2, "Runeboggle");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden");
    }

    @Test
    @DisplayName("Leaves the spell on the stack when its controller pays {1} and draws a card")
    void doesNotCounterWhenControllerPaysAndDraws() {
        harness.setLibrary(player2, List.of(new GhostWarden()));

        GhostWarden warden = new GhostWarden();
        harness.setHand(player1, List.of(warden));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new Runeboggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warden.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Ghost Warden");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ghost Warden");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var warden = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        harness.setHand(player2, List.of(new Runeboggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, warden.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when its target has already been countered")
    void doesNotDrawWhenTargetLeavesStack() {
        harness.setLibrary(player1, List.of(new GhostWarden()));
        harness.setLibrary(player2, List.of(new GhostWarden()));

        GhostWarden warden = new GhostWarden();
        harness.setHand(player1, List.of(warden, new Runeboggle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new Runeboggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warden.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, warden.getId());

        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertInHand(player1, "Ghost Warden");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeboggle");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's own spell and draw a card")
    void canCounterOwnSpellAndDraw() {
        harness.setLibrary(player1, List.of(new GhostWarden()));

        GhostWarden warden = new GhostWarden();
        harness.setHand(player1, List.of(warden, new Runeboggle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, warden.getId());

        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertInGraveyard(player1, "Runeboggle");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden");
        assertThat(gd.stack).isEmpty();
    }
}
