package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicCollusion.class, GrizzlyBears.class, Plains.class, Swamp.class})
class DemonicCollusionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Demonic Collusion offers every card in the library")
    void offersEveryLibraryCard() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new GrizzlyBears()));
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
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(handNames(player1)).containsExactly(chosenName);
        harness.assertInGraveyard(player1, "Demonic Collusion");
    }

    @Test
    @DisplayName("Discarding two cards for buyback returns Demonic Collusion to hand")
    void discardBuybackReturnsToHand() {
        DemonicCollusion spell = new DemonicCollusion();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(spell, new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithDiscardBuyback(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(handNames(player1)).containsExactlyInAnyOrder("Demonic Collusion", "Grizzly Bears");
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

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(Card::getName).toList();
    }
}
