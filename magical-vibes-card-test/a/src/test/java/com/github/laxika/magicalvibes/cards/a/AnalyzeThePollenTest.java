package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EscapeTunnel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AnalyzeThePollen.class, Forest.class, EscapeTunnel.class, NervousGardener.class, Shock.class})
class AnalyzeThePollenTest extends BaseCardTest {

    @Test
    @DisplayName("Without collected evidence, offers only basic lands")
    void withoutEvidenceOffersOnlyBasicLands() {
        setupLibrary();
        cast(List.of());
        harness.passBothPriorities();

        List<Card> offered = librarySearch().params().cards();
        assertThat(offered).extracting(Card::getClass).containsExactly(Forest.class);
    }

    @Test
    @DisplayName("Collected evidence offers creatures and lands")
    void withEvidenceOffersCreaturesAndLands() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of(0, 1, 2, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);

        harness.passBothPriorities();

        assertThat(librarySearch().params().cards()).extracting(Card::getClass)
                .containsExactlyInAnyOrder(Forest.class, EscapeTunnel.class, NervousGardener.class);
    }

    @Test
    @DisplayName("Collected evidence can tutor a creature")
    void withEvidenceCanTutorCreature() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        List<Card> offered = librarySearch().params().cards();
        Card creature = offered.stream()
                .filter(card -> card instanceof NervousGardener)
                .findFirst()
                .orElseThrow();
        int creatureIndex = offered.indexOf(creature);
        harness.handleCardChosen(player1, creatureIndex);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    void withoutEvidencePutsRevealedBasicLandIntoHand() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Shock()));
        cast(List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land).hasSize(1);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals Forest"));
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("Library is shuffled"));
    }

    @Test
    void withEvidenceCanTutorNonbasicLand() {
        EscapeTunnel land = new EscapeTunnel();
        harness.setGraveyard(player1, List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener()));
        harness.setLibrary(player1, List.of(land, new Shock()));
        cast(List.of(0, 1, 2, 3));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land).hasSize(1);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals Escape Tunnel"));
    }

    @Test
    void canDeclineEvidenceEvenWhenEnoughIsAvailable() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(librarySearch().params().cards()).extracting(Card::getClass)
                .containsExactly(Forest.class);
    }

    @Test
    void canCollectMoreThanEightIncludingNoncreaturesAndZeroManaValueCards() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new NervousGardener(), new Shock(), new Forest());
        harness.setGraveyard(player1, evidence);
        setupLibrary();
        cast(List.of(0, 1, 2, 3, 4, 5));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
        assertThat(librarySearch().params().cards()).extracting(Card::getClass)
                .containsExactlyInAnyOrder(Forest.class, EscapeTunnel.class, NervousGardener.class);
    }

    @Test
    void cannotCollectLessThanEight() {
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener(),
                new NervousGardener(), new Shock());
        harness.setGraveyard(player1, evidence);

        assertThatThrownBy(() -> cast(List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCountTheSameEvidenceCardRepeatedly() {
        NervousGardener evidence = new NervousGardener();
        harness.setGraveyard(player1, List.of(evidence));

        assertThatThrownBy(() -> cast(List.of(0, 0, 0, 0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFailToFindEvenWhenBasicLandIsAvailable() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        cast(List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("Library is shuffled"));
        harness.assertInGraveyard(player1, "Analyze the Pollen");
    }

    @Test
    void resolvesWithoutFindingWhenLibraryHasNoBasicLand() {
        EscapeTunnel land = new EscapeTunnel();
        NervousGardener creature = new NervousGardener();
        harness.setLibrary(player1, List.of(land, creature));
        cast(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        harness.assertInGraveyard(player1, "Analyze the Pollen");
    }

    private void cast(List<Integer> evidenceIndices) {
        harness.setHand(player1, List.of(new AnalyzeThePollen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, evidenceIndices);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new EscapeTunnel(), new NervousGardener(), new Shock()));
    }

    private PendingInteraction.LibrarySearch librarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
