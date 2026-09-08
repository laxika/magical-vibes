package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriceOfFreedom.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class, Island.class})
class PriceOfFreedomTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's artifact, offers its controller a tapped basic, and draws a card")
    void destroysArtifactSearchesForTappedBasicAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        chooseLibraryCard(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys an opponent's land and searches for a tapped basic land")
    void destroysLandAndSearchesForTappedBasic() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        chooseLibraryCard(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroyed permanent's controller may decline the search and the caster still draws")
    void mayDeclineSearchAndCasterStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        chooseLibraryCard(player2, -1);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an artifact controlled by the caster")
    void cannotTargetOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        givePriceOfFreedom();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        givePriceOfFreedom();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    private void givePriceOfFreedom() {
        harness.setHand(player1, List.of(new PriceOfFreedom()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void chooseLibraryCard(com.github.laxika.magicalvibes.model.Player player, int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player, new InteractionAnswer.LibraryCardChosen(index));
    }
}
