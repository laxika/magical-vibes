package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.n.Nourish;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vex.class, Nourish.class, DarksteelCitadel.class})
class VexTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and offers its controller a card draw")
    void targetSpellControllerMayDraw() {
        Nourish nourish = new Nourish();
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, nourish, "{G}{G}");

        harness.setHand(player2, List.of(new Vex()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nourish.getId());

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nourish");
        harness.assertInGraveyard(player2, "Vex");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters the target spell when its controller declines the draw")
    void targetSpellControllerMayDeclineDraw() {
        Nourish nourish = new Nourish();
        DarksteelCitadel libraryCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, nourish, "{G}{G}");

        harness.setHand(player2, List.of(new Vex()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nourish.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nourish");
        harness.assertInGraveyard(player2, "Vex");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("The target is countered before its controller chooses whether to draw")
    void countersBeforeOfferingDraw() {
        Nourish nourish = new Nourish();
        harness.setLibrary(player1, List.of(new DarksteelCitadel()));
        harness.castFromHand(player1, nourish, "{G}{G}");
        harness.setHand(player2, List.of(new Vex()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nourish.getId());

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Nourish");

        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player2, "Vex");
    }

    @Test
    @DisplayName("Can counter its controller's own spell and offer that player the draw")
    void countersOwnSpell() {
        Nourish nourish = new Nourish();
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, nourish, "{G}{G}");
        harness.setHand(player1, List.of(new Vex()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, nourish.getId());

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Nourish");
        harness.assertInGraveyard(player1, "Vex");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not offer a draw when the target spell has already left the stack")
    void missingTargetDoesNotOfferDraw() {
        Nourish nourish = new Nourish();
        DarksteelCitadel libraryCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, nourish, "{G}{G}");
        harness.setHand(player2, List.of(new Vex(), new Vex()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nourish.getId());
        harness.castAndResolveInstant(player2, 0, nourish.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Nourish");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
