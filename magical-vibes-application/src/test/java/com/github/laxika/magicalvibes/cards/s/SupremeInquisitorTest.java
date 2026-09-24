package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.r.RiptideBiologist;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupremeInquisitor.class, RiptideBiologist.class, GlorySeeker.class})
class SupremeInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to five cards from the target player's library")
    void exilesUpToFiveCards() {
        Permanent inquisitor = prepareWithFiveWizards();
        harness.setLibrary(player2, List.of(
                new GlorySeeker(), new GlorySeeker(), new GlorySeeker(),
                new GlorySeeker(), new GlorySeeker(), new GlorySeeker()));

        activate(inquisitor, player2.getId());
        harness.passBothPriorities();

        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(5);
        assertThat(gd.exiledCards.stream()
                .filter(entry -> entry.ownerId().equals(player2.getId()))
                .toList())
                .noneMatch(ExiledCardEntry::faceDown);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTapped)
                .count()).isEqualTo(5);
        assertThat(inquisitor.isTapped()).isTrue();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
    }

    @Test
    @DisplayName("The controller may stop after exiling fewer than five cards")
    void mayStopEarly() {
        Permanent inquisitor = prepareWithFiveWizards();
        harness.setLibrary(player2, List.of(
                new GlorySeeker(), new GlorySeeker(), new GlorySeeker()));

        activate(inquisitor, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot activate without five untapped Wizards")
    void requiresFiveUntappedWizards() {
        Permanent inquisitor = addCreatureReady(player1, new SupremeInquisitor());
        addReadyRiptideBiologists(3);
        addCreatureReady(player1, new GlorySeeker());

        assertThatThrownBy(() -> activate(inquisitor, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when one of five Wizards is tapped")
    void cannotActivateWithTappedWizard() {
        Permanent inquisitor = addCreatureReady(player1, new SupremeInquisitor());
        Permanent tappedWizard = addCreatureReady(player1, new RiptideBiologist());
        addReadyRiptideBiologists(3);
        tappedWizard.tap();

        assertThatThrownBy(() -> activate(inquisitor, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only a player")
    void cannotTargetPermanent() {
        Permanent inquisitor = prepareWithFiveWizards();
        Permanent permanent = addCreatureReady(player2, new GlorySeeker());

        assertThatThrownBy(() -> activate(inquisitor, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("An empty target library is still shuffled without a search prompt")
    void emptyTargetLibrary() {
        prepareWithFiveWizards();
        harness.setLibrary(player2, List.of());

        activate(gd.playerBattlefields.get(player1.getId()).getFirst(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText()
                .contains("searches " + gd.playerIdToName.get(player2.getId())
                        + "'s library but it is empty. Library is shuffled."));
    }

    @Test
    @DisplayName("Can target its controller's library")
    void targetsControllersLibrary() {
        prepareWithFiveWizards();
        harness.setLibrary(player1, List.of(new GlorySeeker(), new GlorySeeker()));

        activate(gd.playerBattlefields.get(player1.getId()).getFirst(), player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private Permanent prepareWithFiveWizards() {
        Permanent inquisitor = addCreatureReady(player1, new SupremeInquisitor());
        addReadyRiptideBiologists(5);
        return inquisitor;
    }

    private void addReadyRiptideBiologists(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new RiptideBiologist());
        }
    }

    private void activate(Permanent inquisitor, java.util.UUID targetId) {
        int inquisitorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(inquisitor);
        harness.activateAbility(player1, inquisitorIndex, null, targetId);

        List<Permanent> wizards = new ArrayList<>(gd.playerBattlefields.get(player1.getId()))
                .subList(0, 5);
        for (Permanent wizard : wizards) {
            harness.handlePermanentChosen(player1, wizard.getId());
        }
    }
}
