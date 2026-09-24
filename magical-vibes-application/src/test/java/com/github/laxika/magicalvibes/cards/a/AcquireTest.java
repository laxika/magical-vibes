package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EarlyFrost;
import com.github.laxika.magicalvibes.cards.g.GraftedWargear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Acquire.class, EarlyFrost.class, GraftedWargear.class})
class AcquireTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen artifact from an opponent's library onto the battlefield under your control")
    void putsChosenArtifactOntoBattlefieldUnderYourControl() {
        harness.setLibrary(player2, List.of(new EarlyFrost(), new GraftedWargear()));
        harness.setHand(player1, List.of(new Acquire()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grafted Wargear");
        harness.assertNotOnBattlefield(player2, "Grafted Wargear");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .anyMatch(card -> card.getName().equals("Early Frost"));
        assertThat(gameLogContains(gd.playerIdToName.get(player2.getId()) + "'s library is shuffled.")).isTrue();
        harness.assertInGraveyard(player1, "Acquire");
    }

    @Test
    @DisplayName("May decline to find an artifact")
    void mayDeclineToFindArtifact() {
        harness.setLibrary(player2, List.of(new GraftedWargear(), new EarlyFrost()));
        harness.setHand(player1, List.of(new Acquire()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grafted Wargear");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2)
                .anyMatch(card -> card.getName().equals("Grafted Wargear"));
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Acquire");
    }

    @Test
    @DisplayName("Does not prompt when the target library has no artifact")
    void noArtifactDoesNotPrompt() {
        harness.setLibrary(player2, List.of(new EarlyFrost()));
        harness.setHand(player1, List.of(new Acquire()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Grafted Wargear");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .anyMatch(card -> card.getName().equals("Early Frost"));
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        harness.assertInGraveyard(player1, "Acquire");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new Acquire()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
