package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.MightSliver;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicCollusion.class, Cancel.class, MightSliver.class, Plains.class, Swamp.class})
class DemonicCollusionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Demonic Collusion offers every card in the library")
    void offersEveryLibraryCard() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new MightSliver()));
        harness.setHand(player1, List.of(new DemonicCollusion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and Demonic Collusion into the graveyard")
    void choosesCardToHand() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.setHand(player1, List.of(new DemonicCollusion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(handNames(player1)).containsExactly(chosenName);
        harness.assertInGraveyard(player1, "Demonic Collusion");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library resolves without a card choice")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DemonicCollusion(), new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(handNames(player1)).containsExactly("Demonic Collusion");
        assertThat(graveyardNames(player1)).containsExactlyInAnyOrder("Plains", "Swamp");
    }

    @Test
    @DisplayName("Discarding two cards for buyback returns Demonic Collusion to hand")
    void discardBuybackReturnsToHand() {
        DemonicCollusion spell = new DemonicCollusion();
        harness.setLibrary(player1, List.of(new MightSliver()));
        harness.setHand(player1, List.of(spell, new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(handNames(player1)).containsExactlyInAnyOrder("Demonic Collusion", "Might Sliver");
        assertThat(graveyardNames(player1)).containsExactlyInAnyOrder("Plains", "Swamp");
    }

    @Test
    @DisplayName("Buyback requires two cards to discard")
    void buybackRequiresTwoCards() {
        DemonicCollusion spell = new DemonicCollusion();
        harness.setHand(player1, List.of(spell, new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Demonic Collusion", "Plains");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    @DisplayName("Buyback discards are paid before resolution and the spell waits for the search choice")
    void buybackCostsArePaidBeforeResolution() {
        harness.setLibrary(player1, List.of(new MightSliver()));
        harness.setHand(player1, List.of(new Plains(), new DemonicCollusion(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithDiscardBuyback(player1, 1, null, List.of(2, 0));

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactlyInAnyOrder("Plains", "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertNotInHand(player1, "Demonic Collusion");
        harness.assertNotInGraveyard(player1, "Demonic Collusion");

        harness.handleCardChosen(player1, 0);

        assertThat(handNames(player1)).containsExactlyInAnyOrder("Demonic Collusion", "Might Sliver");
        assertThat(graveyardNames(player1)).containsExactlyInAnyOrder("Plains", "Swamp");
    }

    @Test
    @DisplayName("Countering Demonic Collusion does not return it or refund its buyback discards")
    void counteredSpellDoesNotReturnWithBuyback() {
        DemonicCollusion spell = new DemonicCollusion();
        harness.setLibrary(player1, List.of(new MightSliver()));
        harness.setHand(player1, List.of(spell, new Plains(), new Swamp()));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1, 2));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactlyInAnyOrder("Demonic Collusion", "Plains", "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Might Sliver");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same card cannot pay both buyback discards")
    void buybackRejectsDuplicateDiscard() {
        harness.setHand(player1, List.of(new DemonicCollusion(), new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Demonic Collusion", "Plains", "Swamp");
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    @DisplayName("Demonic Collusion cannot discard itself to pay buyback")
    void buybackRejectsDiscardingItself() {
        harness.setHand(player1, List.of(new DemonicCollusion(), new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Demonic Collusion", "Plains", "Swamp");
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(Card::getName).toList();
    }
}
