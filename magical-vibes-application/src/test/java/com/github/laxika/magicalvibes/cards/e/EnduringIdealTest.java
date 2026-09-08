package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnduringIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Searches an enchantment onto the battlefield and prevents future spell casts")
    void searchesEnchantmentAndPreventsSpellCasts() {
        castEnduringIdeal(List.of(new GloriousAnthem(), new Plains()));

        chooseLibraryCard("Glorious Anthem");

        harness.assertOnBattlefield(player1, "Glorious Anthem");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the spell at the beginning of each upkeep")
    void copiesSpellAtEachUpkeep() {
        castEnduringIdeal(List.of(new GloriousAnthem(), new Plains()));
        chooseLibraryCard("Glorious Anthem");

        harness.setLibrary(player1, List.of(new GloriousAnthem(), new Plains()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        chooseLibraryCard("Glorious Anthem");

        assertThat(findPermanents(player1, "Glorious Anthem")).hasSize(2);
    }

    private void castEnduringIdeal(List<Card> library) {
        harness.setHand(player1, List.of(new EnduringIdeal()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castSorcery(player1, 0, 0);
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
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(cardIndex));
    }
}
