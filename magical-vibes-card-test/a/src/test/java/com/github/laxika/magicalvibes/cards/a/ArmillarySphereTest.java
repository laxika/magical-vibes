package com.github.laxika.magicalvibes.cards.a;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmillarySphere.class, Forest.class, Plains.class, GrizzlyBears.class})
class ArmillarySphereTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Armillary Sphere and offers only basic lands for the search")
    void activatingSacrificesAndOffersBasicLands() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armillary Sphere");
        harness.assertInGraveyard(player1, "Armillary Sphere");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Both chosen basic lands go to hand")
    void bothChosenLandsGoToHand() {
        activateSearch();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // A second pick is offered (up to two).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        // Both basic lands left the library and only the non-land remains.
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canStopAfterOneLandAndStillShuffle() {
        activateSearch();
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Plains", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void cannotTakeMoreThanTwoEvenWithThreeBasicLands() {
        activateSearch();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Plains()));
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName()).containsExactly("Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        activateSearch();
        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Armillary Sphere");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void resolvesWhenOnlyOneBasicLandExists() {
        activateSearch();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void tappedSphereCannotActivate() {
        harness.addToBattlefield(player1, new ArmillarySphere());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Armillary Sphere");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotSacrificeSphere() {
        harness.addToBattlefield(player1, new ArmillarySphere());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Armillary Sphere");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new ArmillarySphere());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new GrizzlyBears()));
    }
}
