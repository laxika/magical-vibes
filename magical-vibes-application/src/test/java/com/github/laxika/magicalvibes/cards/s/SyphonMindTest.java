package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyphonMind.class, GrizzlyBears.class})
class SyphonMindTest extends BaseCardTest {

    @Test
    void opponentDiscardsAndControllerDraws() {
        GrizzlyBears drawn = new GrizzlyBears();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new SyphonMind()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void noCardDiscardedMeansNoDraw() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new SyphonMind()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
