package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicSymbiont.class, GreenwoodSentinel.class, Forest.class})
class PsychicSymbiontTest extends BaseCardTest {

    @Test
    @DisplayName("When Psychic Symbiont enters, target opponent discards and its controller draws")
    void etbDiscardsAndDraws() {
        harness.setHand(player1, List.of(new PsychicSymbiont()));
        harness.setHand(player2, List.of(new GreenwoodSentinel()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForPsychicSymbiont();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
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

        harness.castCreature(player1, 0, player2.getId());
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

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The opponent chooses exactly one card to discard before the controller draws")
    void opponentChoosesOneCardBeforeDraw() {
        harness.setHand(player1, List.of(new PsychicSymbiont()));
        harness.setHand(player2, List.of(new GreenwoodSentinel(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new GreenwoodSentinel()));
        addManaForPsychicSymbiont();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player2, "Greenwood Sentinel");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Psychic Symbiont draws for its controller when the other player casts it")
    void otherControllerDiscardsFromTheirOpponentAndDraws() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new PsychicSymbiont()));
        harness.setLibrary(player2, List.of(new GreenwoodSentinel()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player2, "Psychic Symbiont");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForPsychicSymbiont() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
