package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindblaze.class, DevotedRetainer.class, LanternKami.class, Mountain.class})
class MindblazeTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void castAt(List<Card> library) {
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new Mindblaze()));
        giveMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Mindblaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving prompts the caster for a card name, then a number")
    void promptsForNameThenNumber() {
        castAt(new ArrayList<>(List.of(new DevotedRetainer(), new LanternKami())));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Devoted Retainer");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Deals 8 damage when the library holds exactly the chosen number of copies")
    void dealsEightOnExactMatch() {
        castAt(new ArrayList<>(List.of(
                new DevotedRetainer(), new DevotedRetainer(), new LanternKami())));

        harness.handleListChoice(player1, "Devoted Retainer");
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Deals no damage when the count does not match the chosen number")
    void noDamageOnMiss() {
        castAt(new ArrayList<>(List.of(
                new DevotedRetainer(), new DevotedRetainer(), new LanternKami())));

        harness.handleListChoice(player1, "Devoted Retainer");
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A name with no copies in the library never matches a positive guess")
    void noCopiesNeverMatches() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new DevotedRetainer())));
        castAt(new ArrayList<>(List.of(new LanternKami(), new LanternKami())));

        harness.handleListChoice(player1, "Devoted Retainer");
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals no damage for a positive guess against an empty library")
    void emptyLibraryDoesNotMatchPositiveGuess() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new DevotedRetainer())));
        castAt(new ArrayList<>());

        harness.handleListChoice(player1, "Devoted Retainer");
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a non-positive number choice")
    void rejectsZeroNumberChoice() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new DevotedRetainer())));
        castAt(new ArrayList<>());

        harness.handleListChoice(player1, "Devoted Retainer");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "0"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Rejects a land name choice")
    void rejectsLandNameChoice() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Mountain())));
        castAt(new ArrayList<>(List.of(new Mountain())));

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Mountain"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
