package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttuneWithAether.class, Plains.class, Forest.class, Island.class, KujarSeedsculptor.class})
class AttuneWithAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands to put into hand")
    void resolvesOffersBasicLandsToHand() {
        castAttuneWithAether();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Putting a basic land into hand also gives two energy counters")
    void putsBasicLandIntoHandAndGivesEnergy() {
        castAttuneWithAether();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Attune with Aether");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The found land is revealed and the remaining library is shuffled")
    void revealsFoundLandAndShufflesLibrary() {
        castAttuneWithAether();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals Plains", "into their hand"));
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("Library is shuffled"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Island", "Kujar Seedsculptor");
    }

    @Test
    @DisplayName("Failing to find a present basic land still gives two energy")
    void failingToFindStillGivesEnergy() {
        castAttuneWithAether();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Attune with Aether");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent gaining energy")
    void emptyLibraryStillGivesEnergy() {
        castAttuneWithAether(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Attune with Aether");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library without basic lands still gives energy")
    void noBasicLandsStillGivesEnergy() {
        castAttuneWithAether(List.of(new KujarSeedsculptor()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Attune with Aether");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Energy is added to the controller's existing counters")
    void addsEnergyOnlyToController() {
        gd.setPlayerEnergyCounters(player1.getId(), 3);
        gd.setPlayerEnergyCounters(player2.getId(), 4);
        castAttuneWithAether();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Plains");
    }

    private void castAttuneWithAether() {
        castAttuneWithAether(List.of(new Plains(), new Forest(), new Island(), new KujarSeedsculptor()));
    }

    private void castAttuneWithAether(List<Card> library) {
        harness.setHand(player1, List.of(new AttuneWithAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, library);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
