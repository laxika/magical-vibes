package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSnapjaw;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaSwine;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaSwine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SignalTheClans.class, AdaptiveSnapjaw.class, DiscipleOfTheOldWays.class, ZhurTaaSwine.class, GruulGuildgate.class})
class SignalTheClansTest extends BaseCardTest {

    private void castSignalTheClans(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SignalTheClans()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
    }

    private List<String> offeredNames() {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        return search == null ? null : search.params().cards().stream().map(Card::getName).toList();
    }

    private void pickFromLibrary(String name) {
        List<String> offered = offeredNames();
        int index = offered.indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    private void declinePick() {
        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("Three creature cards with different names put one of them into hand at random")
    void threeDifferentNamesPutOneIntoHand() {
        Card disciple = new DiscipleOfTheOldWays();
        Card snapjaw = new AdaptiveSnapjaw();
        Card swine = new ZhurTaaSwine();
        castSignalTheClans(List.of(disciple, snapjaw, swine, new GruulGuildgate()));

        pickFromLibrary("Disciple of the Old Ways");
        pickFromLibrary("Adaptive Snapjaw");
        pickFromLibrary("Zhur-Taa Swine");

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.getFirst()).isIn(disciple, snapjaw, swine);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(3).doesNotContain(hand.getFirst());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("Only creature cards can be revealed")
    void onlyCreatureCardsAreOffered() {
        castSignalTheClans(List.of(new DiscipleOfTheOldWays(), new GruulGuildgate(), new SignalTheClans()));

        assertThat(offeredNames()).containsExactly("Disciple of the Old Ways");
    }

    @Test
    @DisplayName("Revealing two cards with the same name puts nothing into hand")
    void duplicateNamesPutNothingIntoHand() {
        castSignalTheClans(List.of(new DiscipleOfTheOldWays(), new DiscipleOfTheOldWays(), new ZhurTaaSwine()));

        pickFromLibrary("Disciple of the Old Ways");
        pickFromLibrary("Disciple of the Old Ways");
        pickFromLibrary("Zhur-Taa Swine");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("Stopping short of three cards puts nothing into hand")
    void stoppingEarlyPutsNothingIntoHand() {
        castSignalTheClans(List.of(new DiscipleOfTheOldWays(), new AdaptiveSnapjaw(), new ZhurTaaSwine()));

        pickFromLibrary("Disciple of the Old Ways");
        pickFromLibrary("Adaptive Snapjaw");
        declinePick();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Fewer than three creature cards in the library finds nothing")
    void fewerThanThreeCreaturesFindsNothing() {
        castSignalTheClans(List.of(new DiscipleOfTheOldWays(), new AdaptiveSnapjaw(), new GruulGuildgate()));

        pickFromLibrary("Disciple of the Old Ways");
        pickFromLibrary("Adaptive Snapjaw");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with no creature cards prompts nothing")
    void noCreaturesInLibraryPromptsNothing() {
        castSignalTheClans(List.of(new GruulGuildgate(), new SignalTheClans()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("Declining the first pick leaves every creature in the library")
    void decliningImmediatelyLeavesLibraryIntact() {
        Card disciple = new DiscipleOfTheOldWays();
        Card snapjaw = new AdaptiveSnapjaw();
        Card swine = new ZhurTaaSwine();
        castSignalTheClans(List.of(disciple, snapjaw, swine));

        declinePick();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(disciple, snapjaw, swine);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("An empty library resolves without a search prompt")
    void emptyLibraryResolvesWithoutPrompt() {
        castSignalTheClans(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

}
