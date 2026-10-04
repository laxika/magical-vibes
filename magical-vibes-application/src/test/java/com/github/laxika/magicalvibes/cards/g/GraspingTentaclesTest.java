package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PumpkinBombs;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GraspingTentacles.class, Forest.class, Ornithopter.class, PumpkinBombs.class})
class GraspingTentaclesTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent mills eight, then accepting may returns an artifact under your control")
    void millsThenMayReturnArtifact() {
        Ornithopter artifact = new Ornithopter();
        List<Card> milledCards = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, milledCards);
        harness.setGraveyard(player2, List.of(artifact));

        castGraspingTentacles();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(artifact)
                .containsAll(milledCards)
                .hasSize(9);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .contains(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .doesNotContain(artifact);
    }

    @Test
    @DisplayName("Declining the may leaves the opponent's artifact in their graveyard")
    void decliningMayLeavesArtifact() {
        Ornithopter artifact = new Ornithopter();
        List<Card> milledCards = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, milledCards);
        harness.setGraveyard(player2, List.of(artifact));

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .doesNotContain(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(artifact)
                .hasSize(9);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new GraspingTentacles()));
        addManaForGraspingTentacles();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTakeNewlyMilledNoncreatureArtifactFromShortLibrary() {
        PumpkinBombs artifact = new PumpkinBombs();
        GraspingTentacles nonartifact = new GraspingTentacles();
        harness.setLibrary(player2, List.of(nonartifact, artifact));
        harness.setGraveyard(player2, List.of());

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonartifact);
        Permanent returned = findPermanent(player1, "Pumpkin Bombs");
        assertThat(returned.getCard().getId()).isEqualTo(artifact.getId());
        assertThat(returned.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Pumpkin Bombs");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void casterChoosesOneArtifactFromOpponentsWholeGraveyard() {
        PumpkinBombs oldArtifact = new PumpkinBombs();
        PumpkinBombs milledArtifact = new PumpkinBombs();
        PumpkinBombs ownArtifact = new PumpkinBombs();
        GraspingTentacles nonartifact = new GraspingTentacles();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(nonartifact, oldArtifact));
        harness.setLibrary(player2, List.of(milledArtifact));

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonartifact, oldArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(milledArtifact.getId());
                    assertThat(permanent.isTapped()).isFalse();
                });
        harness.assertNotOnBattlefield(player2, "Pumpkin Bombs");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingWithNoArtifactsFinishesWithoutMovingNonartifacts() {
        GraspingTentacles nonartifact = new GraspingTentacles();
        harness.setLibrary(player2, List.of(nonartifact));
        harness.setGraveyard(player2, List.of());

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonartifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryStillAllowsTakingAnExistingArtifact() {
        PumpkinBombs artifact = new PumpkinBombs();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(artifact));

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Pumpkin Bombs").getCard().getId()).isEqualTo(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsOnlyEightCardsAndLeavesTheRestOfTheLibrary() {
        List<Card> milledCards = List.of(
                new GraspingTentacles(), new GraspingTentacles(),
                new GraspingTentacles(), new GraspingTentacles(),
                new GraspingTentacles(), new GraspingTentacles(),
                new GraspingTentacles(), new GraspingTentacles());
        GraspingTentacles remainingCard = new GraspingTentacles();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(milledCards);
        library.add(remainingCard);
        harness.setLibrary(player2, library);
        harness.setGraveyard(player2, List.of());

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(milledCards);
    }

    private void castGraspingTentacles() {
        harness.setHand(player1, List.of(new GraspingTentacles()));
        addManaForGraspingTentacles();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void addManaForGraspingTentacles() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
