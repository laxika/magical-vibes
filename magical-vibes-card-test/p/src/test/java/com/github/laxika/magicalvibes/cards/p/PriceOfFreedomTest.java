package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

        harness.handleCardChosen(player2, 0);

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
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroyed permanent's controller may decline the search and the caster still draws")
    void mayDeclineSearchAndCasterStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        List<com.github.laxika.magicalvibes.model.Card> library = List.of(new Forest(), new Island(), new GrizzlyBears());
        harness.setLibrary(player2, library);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);

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

    @Test
    @DisplayName("A target that leaves the battlefield prevents both the search and the draw")
    void missingTargetDoesNotDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Price of Freedom");
    }

    @Test
    @DisplayName("Regeneration does not prevent the controller from searching or the caster from drawing")
    void regeneratedArtifactStillAllowsSearchAndDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        target.setRegenerationShield(1);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The caster still draws when the opponent searches but has no basic lands")
    void noBasicLandsStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        givePriceOfFreedom();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }
    private void givePriceOfFreedom() {
        harness.setHand(player1, List.of(new PriceOfFreedom()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
