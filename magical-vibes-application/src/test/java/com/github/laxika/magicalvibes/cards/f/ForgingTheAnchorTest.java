package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.r.RocHunter;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.ReconstructedThopter;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgingTheAnchor.class, EnergyRefractor.class, RocHunter.class,
        Island.class, ReconstructedThopter.class, GiantGrowth.class})
class ForgingTheAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Offers every artifact card among the top five")
    void offersEveryArtifactAmongTopFive() {
        EnergyRefractor refractor = new EnergyRefractor();
        ReconstructedThopter thopter = new ReconstructedThopter();
        setupTopFive(List.of(refractor, new RocHunter(), thopter, new GiantGrowth(), new Island()));
        cast();

        PendingInteraction.LibraryRevealChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(refractor.getId(), thopter.getId());
    }

    @Test
    @DisplayName("Puts multiple chosen artifacts into hand and the rest on the bottom")
    void choosesMultipleArtifactsAndBottomsRest() {
        EnergyRefractor refractor = new EnergyRefractor();
        ReconstructedThopter thopter = new ReconstructedThopter();
        setupTopFive(List.of(refractor, new RocHunter(), thopter, new GiantGrowth(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(refractor.getId(), thopter.getId()));

        harness.assertInHand(player1, "Energy Refractor");
        harness.assertInHand(player1, "Reconstructed Thopter");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Giant Growth", "Island", "Roc Hunter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose no artifacts and bottom all five cards")
    void mayChooseNoArtifacts() {
        setupTopFive(List.of(new EnergyRefractor(), new RocHunter(), new ReconstructedThopter(), new GiantGrowth(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Energy Refractor", "Reconstructed Thopter");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Energy Refractor", "Roc Hunter", "Reconstructed Thopter", "Giant Growth", "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May keep only one artifact and bottoms the unchosen artifact")
    void mayKeepOnlyOneArtifact() {
        EnergyRefractor kept = new EnergyRefractor();
        ReconstructedThopter declined = new ReconstructedThopter();
        Island untouched = new Island();
        setupTopFive(List.of(kept, declined, new RocHunter(), new GiantGrowth(), new Island(), untouched));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(kept);
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, 5)).contains(declined).doesNotContain(kept);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Handles a library with fewer than five cards")
    void handlesShortLibrary() {
        EnergyRefractor artifact = new EnergyRefractor();
        Island land = new Island();
        setupTopFive(List.of(artifact, land));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoms cards automatically when none of the top five are artifacts")
    void noArtifactsAmongTopFive() {
        List<Card> top = List.of(new Island(), new RocHunter(), new GiantGrowth(), new Island(), new RocHunter());
        EnergyRefractor sixth = new EnergyRefractor();
        harness.setLibrary(player1, List.of(top.get(0), top.get(1), top.get(2), top.get(3), top.get(4), sixth));
        cast();

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(sixth);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(top);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves with an empty library without requesting a choice")
    void handlesEmptyLibrary() {
        setupTopFive(List.of());
        cast();

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forging the Anchor");
    }

    @Test
    @DisplayName("May take all five artifacts")
    void mayTakeAllFiveArtifacts() {
        List<Card> artifacts = List.of(new EnergyRefractor(), new ReconstructedThopter(),
                new EnergyRefractor(), new ReconstructedThopter(), new EnergyRefractor());
        harness.setLibrary(player1, artifacts);
        cast();

        harness.handleMultipleCardsChosen(player1, artifacts.stream().map(Card::getId).toList());

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(artifacts);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Publicly discloses chosen artifacts but keeps the other looked-at cards private")
    void disclosesOnlyChosenArtifacts() {
        EnergyRefractor chosen = new EnergyRefractor();
        harness.setLibrary(player1, List.of(chosen, new ReconstructedThopter(),
                new RocHunter(), new GiantGrowth(), new Island()));
        harness.getGameData().gameLog.clear();
        cast();

        assertThat(harness.getGameData().gameLog.stream().map(entry -> entry.plainText()))
                .noneMatch(text -> text.contains("Energy Refractor") || text.contains("Reconstructed Thopter")
                        || text.contains("Roc Hunter") || text.contains("Giant Growth") || text.contains("Island"));

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(harness.getGameData().gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(text -> text.contains("Energy Refractor"))
                .noneMatch(text -> text.contains("Reconstructed Thopter") || text.contains("Roc Hunter")
                        || text.contains("Giant Growth") || text.contains("Island"));
    }

    private void cast() {
        harness.setHand(player1, List.of(new ForgingTheAnchor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
