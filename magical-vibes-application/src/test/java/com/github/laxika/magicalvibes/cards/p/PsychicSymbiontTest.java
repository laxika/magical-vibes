package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsychicSymbiontTest extends BaseCardTest {

    @Test
    @DisplayName("When Psychic Symbiont enters, target opponent discards and its controller draws")
    void etbDiscardsAndDraws() {
        harness.setHand(player1, List.of(new PsychicSymbiont()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForPsychicSymbiont();

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Psychic Symbiont");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Psychic Symbiont still draws when the target opponent has no cards")
    void etbDrawsWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new PsychicSymbiont()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForPsychicSymbiont();

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Psychic Symbiont cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new PsychicSymbiont()));
        addManaForPsychicSymbiont();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void addManaForPsychicSymbiont() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
