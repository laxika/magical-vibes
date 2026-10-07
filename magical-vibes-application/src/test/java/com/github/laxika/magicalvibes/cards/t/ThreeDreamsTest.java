package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.d.DreamLeash;
import com.github.laxika.magicalvibes.cards.f.FistsOfIronwood;
import com.github.laxika.magicalvibes.cards.f.FlightOfFancy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeDreams.class, DreamLeash.class, FlightOfFancy.class, FistsOfIronwood.class, BorosRecruit.class})
class ThreeDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to three Aura cards with different names and reveals them")
    void offersDifferentNamedAuras() {
        setupLibrary(new DreamLeash(), new DreamLeash(), new FlightOfFancy(), new FistsOfIronwood(), new BorosRecruit());
        cast();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);
        assertThat(search.params().cards()).allMatch(Card::isAura);
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().requireDifferentNames()).isTrue();
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Puts three differently named Auras into hand")
    void choosesThreeDifferentAuras() {
        setupLibrary(new DreamLeash(), new DreamLeash(), new FlightOfFancy(), new FistsOfIronwood());
        cast();

        harness.passBothPriorities();
        chooseCard("Dream Leash");

        assertThat(offeredNames()).doesNotContain("Dream Leash");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount())
                .isEqualTo(2);

        chooseCard("Flight of Fancy");
        chooseCard("Fists of Ironwood");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).contains("Dream Leash", "Flight of Fancy", "Fists of Ironwood");
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Can choose fewer than three differently named Auras")
    void choosesFewerThanThreeAuras() {
        DreamLeash dreamLeash = new DreamLeash();
        FlightOfFancy flightOfFancy = new FlightOfFancy();
        BorosRecruit borosRecruit = new BorosRecruit();
        setupLibrary(dreamLeash, flightOfFancy, borosRecruit);
        cast();

        harness.passBothPriorities();
        chooseCard("Dream Leash");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Dream Leash");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(flightOfFancy, borosRecruit);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Does not create an interaction when the library has no Aura")
    void noAuraNoInteraction() {
        setupLibrary(new BorosRecruit());
        cast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Can choose zero Auras even when matching cards are available")
    void choosesZeroAuras() {
        DreamLeash dreamLeash = new DreamLeash();
        FlightOfFancy flightOfFancy = new FlightOfFancy();
        setupLibrary(dreamLeash, flightOfFancy);
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(dreamLeash, flightOfFancy);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Finishes when only duplicate names remain and leaves those duplicates in the library")
    void exhaustsDistinctNames() {
        DreamLeash chosen = new DreamLeash();
        DreamLeash duplicate = new DreamLeash();
        BorosRecruit nonAura = new BorosRecruit();
        setupLibrary(chosen, duplicate, nonAura);
        cast();
        harness.passBothPriorities();

        chooseCard("Dream Leash");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Dream Leash");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(duplicate, nonAura);
        assertThat(gameLogContains("reveals Dream Leash")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Resolves and shuffles an empty library without offering a choice")
    void emptyLibrary() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void cast() {
        harness.castFromHand(player1, new ThreeDreams(), "{4}{W}");
    }

    private void setupLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void chooseCard(String name) {
        List<Card> cards = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        int index = cards.stream().map(Card::getName).toList().indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    private List<String> offeredNames() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .map(Card::getName)
                .toList();
    }

    private List<String> handNames() {
        return gd.playerHands.get(player1.getId()).stream().map(Card::getName).toList();
    }
}
