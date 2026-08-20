package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers instant, sorcery, and Dragon cards among the top six")
    void etbOffersMatchingCards() {
        Card shock = new Shock();
        Card divination = new Divination();
        Card dragon = new ShivanDragon();
        setupTopCards(List.of(shock, new GrizzlyBears(), divination, new Island(), dragon, new GrizzlyBears()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), divination.getId(), dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapped Dragons you control have hexproof")
    void grantsHexproofToOwnUntappedDragons() {
        harness.addToBattlefield(player1, new Dragonologist());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentDragon, Keyword.HEXPROOF)).isFalse();

        dragon.tap();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();

        dragon.untap();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    private void setupTopCards(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new Dragonologist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
