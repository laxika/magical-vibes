package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DenseCanopy;
import com.github.laxika.magicalvibes.cards.i.IdeasUnbound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringIdeal.class, DenseCanopy.class, IdeasUnbound.class})
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

    private void castEnduringIdeal(List<Card> library) {
        harness.setHand(player1, List.of(new EnduringIdeal()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
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
