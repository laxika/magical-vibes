package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DenseCanopy;
import com.github.laxika.magicalvibes.cards.i.IdeasUnbound;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringIdeal.class, DenseCanopy.class, IdeasUnbound.class, Twincast.class})
class EnduringIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Searches an enchantment onto the battlefield and prevents future spell casts")
    void searchesEnchantmentAndPreventsSpellCasts() {
        castEnduringIdeal(List.of(new DenseCanopy(), new IdeasUnbound()));

        chooseLibraryCard("Dense Canopy");

        harness.assertOnBattlefield(player1, "Dense Canopy");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Ideas Unbound");

        harness.setHand(player1, List.of(new IdeasUnbound()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the spell at the beginning of each upkeep")
    void copiesSpellAtEachUpkeep() {
        castEnduringIdeal(List.of(new DenseCanopy(), new IdeasUnbound()));
        chooseLibraryCard("Dense Canopy");

        harness.setLibrary(player1, List.of(new DenseCanopy(), new IdeasUnbound()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        chooseLibraryCard("Dense Canopy");

        assertThat(findPermanents(player1, "Dense Canopy")).hasSize(2);
    }

    @Test
    @DisplayName("Still registers Epic when no enchantment is found")
    void registersEpicWhenNoEnchantmentIsFound() {
        castEnduringIdeal(List.of(new IdeasUnbound()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Ideas Unbound");

        harness.setHand(player1, List.of(new IdeasUnbound()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }


    @Test
    @DisplayName("Epic puts a delayed trigger on the stack before creating its spell copy")
    void upkeepTriggerResolvesBeforeSpellCopyIsCreated() {
        castEnduringIdeal(List.of(new IdeasUnbound()));
        harness.setLibrary(player1, List.of(new DenseCanopy(), new IdeasUnbound()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().isCopy()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        chooseLibraryCard("Dense Canopy");
        harness.assertOnBattlefield(player1, "Dense Canopy");
    }

    @Test
    @DisplayName("A copied original Enduring Ideal also establishes epic for its controller")
    void copiedOriginalEstablishesEpic() {
        EnduringIdeal ideal = new EnduringIdeal();
        harness.setHand(player1, List.of(ideal));
        harness.setLibrary(player1, List.of(new IdeasUnbound()));
        harness.setLibrary(player2, List.of(new IdeasUnbound()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ideal.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, ideal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new DenseCanopy(), new IdeasUnbound()));
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Dense Canopy");
    }

    @Test
    @DisplayName("A restricted search may fail to find even with an enchantment available")
    void canDeclineAvailableEnchantmentAndStillEstablishEpic() {
        castEnduringIdeal(List.of(new DenseCanopy(), new IdeasUnbound()));
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Dense Canopy")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Dense Canopy", "Ideas Unbound");
        harness.setHand(player1, List.of(new IdeasUnbound()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Epic does not copy Enduring Ideal during the opponent's upkeep")
    void noCopyDuringOpponentsUpkeep() {
        castEnduringIdeal(List.of(new IdeasUnbound()));
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castEnduringIdeal(List<Card> library) {
        harness.setHand(player1, List.of(new EnduringIdeal()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void chooseLibraryCard(String cardName) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int cardIndex = IntStream.range(0, search.params().cards().size())
                .filter(i -> cardName.equals(search.params().cards().get(i).getName()))
                .findFirst()
                .orElseThrow();
        harness.handleCardChosen(player1, cardIndex);
    }
}
