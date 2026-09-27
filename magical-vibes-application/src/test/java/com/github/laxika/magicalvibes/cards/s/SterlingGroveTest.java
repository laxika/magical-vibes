package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SterlingGrove.class, DuelingGrounds.class, LlanowarElite.class, Plains.class})
class SterlingGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Other enchantments you control have shroud")
    void otherEnchantmentsYouControlHaveShroud() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new SterlingGrove());
        Permanent otherEnchantment = harness.addToBattlefieldAndReturn(player1, new DuelingGrounds());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElite());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new DuelingGrounds());

        assertThat(gqs.hasKeyword(gd, grove, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherEnchantment, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEnchantment, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Sterling Grove searches for an enchantment and puts it on top")
    void sacrificeSearchesForEnchantmentToTop() {
        harness.addToBattlefield(player1, new SterlingGrove());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        List<Card> library = List.of(new DuelingGrounds(), new LlanowarElite(), new Plains());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sterling Grove");
        harness.assertInGraveyard(player1, "Sterling Grove");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ENCHANTMENT));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Shuffles without a prompt when the library has no enchantment")
    void noEnchantmentInLibrary() {
        harness.addToBattlefield(player1, new SterlingGrove());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        List<Card> library = List.of(new LlanowarElite(), new Plains());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library);
        assertThat(gameLogContains("finds no")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }
}
