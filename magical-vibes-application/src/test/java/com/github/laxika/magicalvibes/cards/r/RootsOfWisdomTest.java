package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.m.MammothGrowth;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootsOfWisdom.class, SnowCoveredForest.class, RavenousLindwurm.class, JasperaSentinel.class, MammothGrowth.class})
class RootsOfWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards and returns a land from the graveyard")
    void returnsLandFromGraveyard() {
        SnowCoveredForest forest = new SnowCoveredForest();
        setGraveyard(forest);
        setLibrary(new MammothGrowth(), new RavenousLindwurm(), new MammothGrowth());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Snow-Covered Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mammoth Growth", "Ravenous Lindwurm", "Mammoth Growth", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Returns an Elf from the graveyard")
    void returnsElfFromGraveyard() {
        JasperaSentinel elves = new JasperaSentinel();
        setGraveyard(elves);
        setLibrary(new MammothGrowth(), new RavenousLindwurm(), new MammothGrowth());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Jaspera Sentinel");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mammoth Growth", "Ravenous Lindwurm", "Mammoth Growth", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Draws a card when no land or Elf is in the graveyard")
    void drawsWhenNoReturnableCardExists() {
        setGraveyard(new RavenousLindwurm());
        SnowCoveredForest forest = new SnowCoveredForest();
        setLibrary(new MammothGrowth(), new RavenousLindwurm(), new MammothGrowth(), forest);

        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Snow-Covered Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Ravenous Lindwurm", "Mammoth Growth", "Ravenous Lindwurm", "Mammoth Growth", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Can return a newly milled land without drawing the fourth card")
    void returnsNewlyMilledLand() {
        SnowCoveredForest land = new SnowCoveredForest();
        MammothGrowth fourth = new MammothGrowth();
        setGraveyard();
        setLibrary(new MammothGrowth(), land, new RavenousLindwurm(), fourth);

        castAndResolve();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mammoth Growth", "Ravenous Lindwurm", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Can return a newly milled Elf even when fewer than three cards remain")
    void returnsNewlyMilledElfFromShortLibrary() {
        JasperaSentinel elf = new JasperaSentinel();
        setGraveyard();
        setLibrary(new MammothGrowth(), elf);

        castAndResolve();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mammoth Growth", "Roots of Wisdom");
    }

    @Test
    @DisplayName("Must return exactly one eligible card and cannot choose a non-Elf creature")
    void returnIsMandatoryAndRestricted() {
        SnowCoveredForest land = new SnowCoveredForest();
        JasperaSentinel elf = new JasperaSentinel();
        MammothGrowth fourth = new MammothGrowth();
        setGraveyard(new RavenousLindwurm(), land, elf);
        setLibrary(new MammothGrowth(), new RavenousLindwurm(), new MammothGrowth(), fourth);

        castAndResolve();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land or Elf does not prevent the fallback draw")
    void ignoresOpponentsGraveyard() {
        SnowCoveredForest land = new SnowCoveredForest();
        JasperaSentinel elf = new JasperaSentinel();
        harness.setGraveyard(player2, List.of(land, elf));
        setGraveyard();
        MammothGrowth drawn = new MammothGrowth();
        setLibrary(new MammothGrowth(), new RavenousLindwurm(), new MammothGrowth(), drawn);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land, elf);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not prevent returning a card already in the graveyard")
    void returnsExistingCardWithEmptyLibrary() {
        SnowCoveredForest land = new SnowCoveredForest();
        setGraveyard(land);
        setLibrary();

        castAndResolve();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Roots of Wisdom");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new RootsOfWisdom()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    private void setGraveyard(Card... cards) {
        harness.setGraveyard(player1, List.of(cards));
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
