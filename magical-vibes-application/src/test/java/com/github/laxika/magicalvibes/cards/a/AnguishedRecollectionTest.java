package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({AnguishedRecollection.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class AnguishedRecollectionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card, then seeks two cards with no shared card type")
    void discardsThenSeeksTwoCardsWithNoSharedCardType() {
        harness.setHand(player1, List.of(new AnguishedRecollection(), new Forest()));
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not seek when the discard leaves no eligible cards")
    void doesNotSeekWhenNoEligibleCardsRemain() {
        harness.setHand(player1, List.of(new AnguishedRecollection(), new Forest()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        harness.assertInGraveyard(player1, "Forest");
    }
}
