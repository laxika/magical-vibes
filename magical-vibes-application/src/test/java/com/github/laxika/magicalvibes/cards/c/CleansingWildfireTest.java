package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleansingWildfire.class, Forest.class, Island.class, GrizzlyBears.class})
class CleansingWildfireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land, offers its controller a tapped basic, and draws a card")
    void destroysLandSearchesForTappedBasicAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castWildfire(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        chooseLibraryCard(player2, 0);

        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroyed land's controller may decline the search and the caster still draws")
    void mayDeclineSearchAndCasterStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castWildfire(target);

        harness.passBothPriorities();
        chooseLibraryCard(player2, -1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleansingWildfire()));
        addWildfireMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    private void castWildfire(Permanent target) {
        harness.setHand(player1, List.of(new CleansingWildfire()));
        addWildfireMana();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void addWildfireMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void chooseLibraryCard(Player player, int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player, new InteractionAnswer.LibraryCardChosen(index));
    }
}
