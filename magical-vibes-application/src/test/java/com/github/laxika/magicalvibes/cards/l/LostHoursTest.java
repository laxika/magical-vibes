package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostHours.class, ZoeticCavern.class, NessianCourser.class})
class LostHoursTest extends BaseCardTest {

    @Test
    void choosesOnlyNonlandCardAndPutsItThirdFromTop() {
        Card land = new ZoeticCavern();
        Card chosen = new NessianCourser();
        Card top = new NessianCourser();
        Card second = new NessianCourser();
        Card below = new NessianCourser();
        harness.setHand(player2, List.of(land, chosen));
        harness.setLibrary(player2, List.of(top, second, below));

        castLostHours();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second, chosen, below);
    }

    @Test
    void putsChosenCardOnBottomWhenLibraryHasFewerThanTwoCards() {
        Card chosen = new NessianCourser();
        Card only = new NessianCourser();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of(only));

        castLostHours();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(only, chosen);
    }

    @Test
    void putsChosenCardOnTopWhenLibraryIsEmpty() {
        Card chosen = new NessianCourser();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of());

        castLostHours();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen);
    }

    @Test
    void doesNothingWhenTargetHasNoNonlandCards() {
        Card land = new ZoeticCavern();
        Card top = new NessianCourser();
        harness.setHand(player2, List.of(land));
        harness.setLibrary(player2, List.of(top));

        castLostHours();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    @Test
    void doesNothingWhenTargetHandIsEmpty() {
        Card top = new NessianCourser();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(top));

        castLostHours();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Lost Hours");
    }

    @Test
    void casterChoosesExactlyOneOfMultipleNonlandCards() {
        Card first = new NessianCourser();
        Card chosen = new NessianCourser();
        Card land = new ZoeticCavern();
        Card top = new NessianCourser();
        Card second = new NessianCourser();
        harness.setHand(player2, List.of(first, land, chosen));
        harness.setLibrary(player2, List.of(top, second));

        castLostHours();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second, chosen);
        harness.assertInGraveyard(player1, "Lost Hours");
        harness.assertNotInGraveyard(player2, "Nessian Courser");
    }

    @Test
    void canTargetItsController() {
        Card chosen = new NessianCourser();
        Card land = new ZoeticCavern();
        Card top = new NessianCourser();
        Card second = new NessianCourser();
        harness.setHand(player1, List.of(new LostHours(), land, chosen));
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second, chosen);
        harness.assertInGraveyard(player1, "Lost Hours");
    }
    private void castLostHours() {
        harness.setHand(player1, List.of(new LostHours()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
