package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostHours.class, Forest.class, GrizzlyBears.class})
class LostHoursTest extends BaseCardTest {

    @Test
    void choosesOnlyNonlandCardAndPutsItThirdFromTop() {
        Card land = new Forest();
        Card chosen = new GrizzlyBears();
        Card top = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card below = new GrizzlyBears();
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
        Card chosen = new GrizzlyBears();
        Card only = new GrizzlyBears();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of(only));

        castLostHours();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(only, chosen);
    }

    @Test
    void doesNothingWhenTargetHasNoNonlandCards() {
        Card land = new Forest();
        Card top = new GrizzlyBears();
        harness.setHand(player2, List.of(land));
        harness.setLibrary(player2, List.of(top));

        castLostHours();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    private void castLostHours() {
        harness.setHand(player1, List.of(new LostHours()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
